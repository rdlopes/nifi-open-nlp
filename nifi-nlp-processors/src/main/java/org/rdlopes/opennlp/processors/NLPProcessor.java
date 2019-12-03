package org.rdlopes.opennlp.processors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnRemoved;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.tools.NLPTool;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static opennlp.tools.ml.AbstractTrainer.VERBOSE_PARAM;
import static opennlp.tools.util.TrainingParameters.*;
import static org.rdlopes.opennlp.common.NLPAttribute.NLP_EVALUATION_ERROR_DESCRIPTION;
import static org.rdlopes.opennlp.common.NLPAttribute.NLP_EVALUATION_ERROR_KEY;
import static org.rdlopes.opennlp.common.NLPProperty.*;

@EventDriven
@SupportsBatching
@EqualsAndHashCode(callSuper = true)
@WritesAttributes({@WritesAttribute(attribute = NLP_EVALUATION_ERROR_KEY, description = NLP_EVALUATION_ERROR_DESCRIPTION)})
public abstract class NLPProcessor<M extends BaseModel, T extends NLPTool<M>> extends BaseProcessor {

    @Getter
    private final boolean trainable;

    @Getter
    private T nlp;

    protected NLPProcessor(boolean trainable) {
        this.trainable = trainable;
    }

    protected abstract T createTool(Path modelPath);

    private TrainingParameters createTrainingParameters(ValidationContext validationContext) {
        final TrainingParameters trainingParameters = TrainingParameters.defaultParams();
        trainingParameters.put(ITERATIONS_PARAM, TRAINABLE_TRAINING_PARAM_ITERATIONS.getStringFrom(validationContext));
        trainingParameters.put(CUTOFF_PARAM, TRAINABLE_TRAINING_PARAM_CUTOFF.getStringFrom(validationContext));
        trainingParameters.put(ALGORITHM_PARAM, TRAINABLE_TRAINING_PARAM_ALGORITHM.getStringFrom(validationContext));
        trainingParameters.put(VERBOSE_PARAM, TRAINABLE_TRAINING_PARAM_VERBOSE.getStringFrom(validationContext));
        trainingParameters.put(THREADS_PARAM, TRAINABLE_TRAINING_PARAM_THREADS.getStringFrom(validationContext));
        trainingParameters.put(TRAINER_TYPE_PARAM, TRAINABLE_TRAINING_PARAM_TYPE.getStringFrom(validationContext));
        return trainingParameters;
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        final String storagePath = COMMON_MODELS_STORAGE_DIRECTORY.getStringFrom(validationContext);
        final Path modelPath = get(storagePath).resolve(getIdentifier() + ".bin");
        getLogger().debug("customValidate | modelPath:{}", new Object[]{modelPath});
        this.nlp = Optional.ofNullable(this.nlp).orElseGet(() -> createTool(modelPath));

        if (this.nlp.modelExists()) {
            return emptyList();
        }

        Collection<ValidationResult> results = new ArrayList<>(super.customValidate(validationContext));
        if (trainable) {
            validateTraining(validationContext, results);
        } else {
            validatePreTrainedModel(validationContext, results);
        }

        getLogger().debug("customValidate | results:{}", new Object[]{results});
        return results;
    }

    @Override
    public void onPropertyModified(final PropertyDescriptor descriptor, final String oldValue, final String newValue) {
        getLogger().debug("onPropertyModified | descriptor: {} | old: {} | new: {}", new Object[]{descriptor, oldValue, newValue});
        Optional.ofNullable(this.nlp)
                .filter(NLPTool::modelExists)
                .ifPresent(NLPTool::removeModel);
    }

    @Override
    public List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return trainable ?
                Stream.of(
                        COMMON_CHARACTER_SET,
                        COMMON_MODELS_STORAGE_DIRECTORY,
                        TRAINABLE_TRAINING_LANGUAGE,
                        TRAINABLE_TRAINING_PARAM_CUTOFF,
                        TRAINABLE_TRAINING_PARAM_ITERATIONS,
                        TRAINABLE_TRAINING_PARAM_ALGORITHM,
                        TRAINABLE_TRAINING_PARAM_VERBOSE,
                        TRAINABLE_TRAINING_PARAM_THREADS,
                        TRAINABLE_TRAINING_PARAM_TYPE,
                        TRAINABLE_TRAINING_FILE_PATH,
                        TRAINABLE_TRAINING_DATA)
                        .map(p -> p.descriptor)
                        .collect(toList()) :
                Stream.of(
                        COMMON_CHARACTER_SET,
                        COMMON_MODELS_STORAGE_DIRECTORY,
                        TRAINED_MODEL_FILE_PATH)
                        .map(p -> p.descriptor)
                        .collect(toList());
    }

    @OnRemoved
    public void onRemoved(ProcessContext context) {
        getLogger().info("Processor removed with context: {}", new Object[]{context});
        nlp.removeModel();
    }

    @OnScheduled
    public void onScheduled(ProcessContext context) {
        getLogger().info("Processor scheduled with context: {}", new Object[]{context});
        // no loading of the model here - to avoid keeping model in memory
    }

    protected void validatePreTrainedModel(ValidationContext validationContext, Collection<ValidationResult> results) {
        final Path trainedModelPath = get(TRAINED_MODEL_FILE_PATH.getStringFrom(validationContext));
        try {
            nlp.createModelFromPreTrained(trainedModelPath);
        } catch (Exception e) {
            getLogger().warn("pre-trained validation error", e);
            results.add(new ValidationResult.Builder()
                    .input(trainedModelPath.toString()).valid(false)
                    .subject("pre-trained").explanation(e.getMessage())
                    .build());
        }
    }

    private void validateTraining(ValidationContext validationContext, Collection<ValidationResult> results) {
        final String trainingLanguage = TRAINABLE_TRAINING_LANGUAGE.getStringFrom(validationContext);
        final TrainingParameters trainingParameters = createTrainingParameters(validationContext);

        if (!TRAINABLE_TRAINING_FILE_PATH.isSetIn(validationContext) && !TRAINABLE_TRAINING_DATA.isSetIn(validationContext)) {
            getLogger().warn("missing training data");
            results.add(new ValidationResult.Builder()
                    .input("file:null | data:null").valid(false)
                    .subject("training").explanation("Trainable processor requires training data or training file")
                    .build());

        } else {
            final String trainingFilePath = TRAINABLE_TRAINING_FILE_PATH.getStringFrom(validationContext);
            final String trainingData = TRAINABLE_TRAINING_DATA.getStringFrom(validationContext);
            try {
                nlp.createModelFromTraining(validationContext, trainingParameters, trainingLanguage, trainingFilePath, trainingData);
            } catch (Exception e) {
                getLogger().warn("training validation error", e);
                results.add(new ValidationResult.Builder()
                        .input(String.format("language:%s | file:%s | data:%s", trainingLanguage, trainingFilePath, trainingData)).valid(false)
                        .subject("training").explanation(e.getMessage())
                        .build());
            }
        }
    }

    @Override
    protected void processInput(ProcessContext processContext, InputStream in, Map<String, String> attributes) {
        final Charset charset = COMMON_CHARACTER_SET.getCharsetFrom(processContext);
        final Map<String, String> evaluation = nlp.processContent(processContext, in, charset, attributes);
        attributes.putAll(evaluation);
    }
}
