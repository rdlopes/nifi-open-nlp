package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.context.PropertyContext;
import org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import static opennlp.tools.ml.AbstractTrainer.VERBOSE_PARAM;
import static opennlp.tools.util.TrainingParameters.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractTrainableProcessor<M extends BaseModel> extends AbstractNLPProcessor<M> {

    protected final boolean modelRequired;

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(TRAINABLE_TRAINING_LANGUAGE.descriptor,
                                                                                                  TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor,
                                                                                                  TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor,
                                                                                                  TRAINABLE_TRAINING_PARAM_ALGORITHM.descriptor,
                                                                                                  TRAINABLE_TRAINING_PARAM_VERBOSE.descriptor,
                                                                                                  TRAINABLE_TRAINING_PARAM_THREADS.descriptor,
                                                                                                  TRAINABLE_TRAINING_PARAM_TYPE.descriptor,
                                                                                                  TRAINABLE_TRAINING_FILE_PATH.descriptor,
                                                                                                  TRAINABLE_TRAINING_DATA.descriptor))
                                                                                .collect(toList());

    public AbstractTrainableProcessor(boolean modelRequired) {
        this.modelRequired = modelRequired;
    }

    private M doTrain(PropertyContext validationContext,
                      Collection<ValidationResult> results,
                      String trainingLanguage,
                      Charset charset,
                      TrainingParameters trainingParameters,
                      String sourceName,
                      InputStreamFactory inputStreamFactory) {
        try {
            return getToolWrapper().trainModel(validationContext, trainingLanguage, charset, trainingParameters, inputStreamFactory);
        } catch (Exception e) {
            getLogger().warn("Training from " + sourceName + " failed", e);
            results.add(new ValidationResult.Builder()
                                .valid(false).input(sourceName)
                                .subject("Training from " + sourceName + " failed")
                                .explanation(e.getMessage()).build());
        }
        return null;
    }

    private void storeModel(String modelsStorageDirectory, M model) throws IOException {
        Path modelFilePath = get(modelsStorageDirectory).resolve(getIdentifier() + ".bin");
        if (!modelFilePath.getParent().toFile().exists()) {
            createDirectory(modelFilePath.getParent());
        }
        deleteIfExists(modelFilePath);
        getLogger().info("Storing model {} at {}", new Object[]{model, modelFilePath});
        model.serialize(modelFilePath);
    }

    @Override
    protected void validateModel(ValidationContext validationContext, Collection<ValidationResult> results, Charset charset, String modelsStorageDirectory) throws IOException {
        M model = null;

        final String trainingLanguage = TRAINABLE_TRAINING_LANGUAGE.getStringFrom(validationContext);

        final TrainingParameters trainingParameters = TrainingParameters.defaultParams();
        trainingParameters.put(ITERATIONS_PARAM, TRAINABLE_TRAINING_PARAM_ITERATIONS.getStringFrom(validationContext));
        trainingParameters.put(CUTOFF_PARAM, TRAINABLE_TRAINING_PARAM_CUTOFF.getStringFrom(validationContext));
        trainingParameters.put(ALGORITHM_PARAM, TRAINABLE_TRAINING_PARAM_ALGORITHM.getStringFrom(validationContext));
        trainingParameters.put(VERBOSE_PARAM, TRAINABLE_TRAINING_PARAM_VERBOSE.getStringFrom(validationContext));
        trainingParameters.put(THREADS_PARAM, TRAINABLE_TRAINING_PARAM_THREADS.getStringFrom(validationContext));
        trainingParameters.put(TRAINER_TYPE_PARAM, TRAINABLE_TRAINING_PARAM_TYPE.getStringFrom(validationContext));

        // train model from training data file, if provided
        if (TRAINABLE_TRAINING_FILE_PATH.isSetIn(validationContext)) {
            final String trainingFilePath = TRAINABLE_TRAINING_FILE_PATH.getStringFrom(validationContext);
            File dataFile = get(trainingFilePath).normalize().toFile();
            getLogger().debug("createModel | dataFile: {}", new Object[]{dataFile});

            try {
                InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(dataFile);
                model = doTrain(validationContext, results, trainingLanguage, charset, trainingParameters,
                                "file", inputStreamFactory);

            } catch (FileNotFoundException e) {
                getLogger().warn("Training file not found", e);
                results.add(new ValidationResult.Builder()
                                    .valid(false).input(dataFile.getAbsolutePath())
                                    .subject("Training file not found")
                                    .explanation(e.getMessage()).build());
            }

        }

        // train model from training data, if provided
        if (TRAINABLE_TRAINING_DATA.isSetIn(validationContext)) {

            String trainingData = TRAINABLE_TRAINING_DATA.getStringFrom(validationContext);
            getLogger().debug("createModel | trainingData: {}", new Object[]{trainingData});

            model = doTrain(validationContext, results, trainingLanguage, charset, trainingParameters,
                            "data", () -> IOUtils.toInputStream(trainingData, charset));
        }

        if (model != null) {
            storeModel(modelsStorageDirectory, model);

        } else if (modelRequired) {
            results.add(new ValidationResult.Builder()
                                .valid(false)
                                .input("<null> model")
                                .subject("NLP model is required")
                                .explanation("This tool requires a model, please provide one.")
                                .build());
        }
    }

}
