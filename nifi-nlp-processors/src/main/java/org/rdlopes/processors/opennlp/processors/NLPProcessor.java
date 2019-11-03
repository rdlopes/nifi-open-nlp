package org.rdlopes.processors.opennlp.processors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.lifecycle.OnRemoved;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.rdlopes.processors.opennlp.tools.NLPTool;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static opennlp.tools.ml.AbstractTrainer.VERBOSE_PARAM;
import static opennlp.tools.util.TrainingParameters.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.COMMON_ERROR;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

@EventDriven
@SupportsBatching
@EqualsAndHashCode(callSuper = true)
public abstract class NLPProcessor<M extends BaseModel, T extends NLPTool<M>> extends AbstractProcessor {
    public static final Relationship RELATIONSHIP_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Parsing completed successfully")
            .build();

    public static final Relationship RELATIONSHIP_UNMATCHED = new Relationship.Builder()
            .name("unmatched")
            .description("Unmatched content")
            .build();

    @Getter
    private final Set<Relationship> relationships = Stream.of(
            RELATIONSHIP_SUCCESS,
            RELATIONSHIP_UNMATCHED).collect(toSet());

    @Getter
    private final boolean trainable;

    @Getter
    private T nlp;

    protected NLPProcessor(boolean trainable) {this.trainable = trainable;}

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
        Collection<ValidationResult> results = new ArrayList<>(super.customValidate(validationContext));

        final String storagePath = COMMON_MODELS_STORAGE_DIRECTORY.getStringFrom(validationContext);
        final Path modelPath = get(storagePath).resolve(getIdentifier() + ".bin");
        this.nlp = createTool(modelPath);
        this.nlp.removeModel();

        if (trainable) {
            validateTraining(validationContext, results);
        } else {
            validatePreTrainedModel(validationContext, results);
        }

        getLogger().debug("customValidate | results:{}", new Object[]{results});
        return results;
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

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession processSession) {
        FlowFile flowFile = Optional.ofNullable(processSession.get())
                                    .orElseGet(processSession::create);
        final ConcurrentMap<String, String> attributes = new ConcurrentHashMap<>(flowFile.getAttributes());
        final Charset charset = COMMON_CHARACTER_SET.getCharsetFrom(processContext);
        Relationship relationship = RELATIONSHIP_UNMATCHED;

        try {
            processSession.read(flowFile, in -> attributes.putAll(nlp.processContent(processContext, in, charset, attributes)));
            flowFile = processSession.putAllAttributes(flowFile, attributes);
            relationship = RELATIONSHIP_SUCCESS;
            getLogger().debug("onTrigger | flow file content evaluated: {}", new Object[]{attributes});

        } catch (Exception e) {
            flowFile = COMMON_ERROR.updateFlowFile(processSession, flowFile, e.getMessage());
            relationship = RELATIONSHIP_UNMATCHED;
            getLogger().warn("Error while evaluating content", e);

        } finally {
            processSession.getProvenanceReporter().route(flowFile, relationship);
            processSession.transfer(flowFile, relationship);
            getLogger().info("Routing {} to {}", new Object[]{flowFile, relationship});
        }
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
                                .input("file:null;data:null").valid(false)
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
                                    .input(String.format("language:%s;file:%s;data:%s", trainingLanguage, trainingFilePath, trainingData)).valid(false)
                                    .subject("training").explanation(e.getMessage())
                                    .build());
            }
        }
    }

}
