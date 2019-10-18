package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.lifecycle.OnRemoved;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toSet;
import static opennlp.tools.ml.AbstractTrainer.VERBOSE_DEFAULT;
import static opennlp.tools.ml.AbstractTrainer.VERBOSE_PARAM;
import static opennlp.tools.util.TrainingParameters.*;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.apache.nifi.processor.util.StandardValidators.*;

@EqualsAndHashCode(callSuper = true)
abstract class AbstractNlpProcessor<M extends BaseModel> extends AbstractProcessor {

    public static final Relationship RELATIONSHIP_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Parsing completed successfully")
            .build();

    public static final Relationship RELATIONSHIP_UNMATCHED = new Relationship.Builder()
            .name("unmatched")
            .description("Unmatched content")
            .build();

    static final String ATTRIBUTE_NLP_ERROR = "nlp.error";

    static final String ATTRIBUTE_NLP_ERROR_DESCRIPTION = "Error message raised by processing the content, if any.";

    static final PropertyDescriptor PROPERTY_MODEL_FILE_PATH = new PropertyDescriptor.Builder()
            .name("Training model file")
            .description("Path to the model for the NLP engine.")
            .required(false)
            .addValidator(NON_BLANK_VALIDATOR)
            .build();

    static final PropertyDescriptor PROPERTY_TRAINING_ALGORITHM = new PropertyDescriptor.Builder()
            .name("Training algorithm")
            .description("Training parameter (ALGORITHM_PARAM).")
            .required(true)
            .allowableValues(
                    new AllowableValue(GISTrainer.MAXENT_VALUE, "Maximum Entropy"),
                    new AllowableValue(NaiveBayesTrainer.NAIVE_BAYES_VALUE, "Naive Bayes classifier")
            )
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue(GISTrainer.MAXENT_VALUE)
            .build();

    static final PropertyDescriptor PROPERTY_TRAINING_CUTOFF = new PropertyDescriptor.Builder()
            .name("Training cut off")
            .description("Training parameter (CUTOFF_PARAM).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue(String.valueOf(5))
            .build();

    static final PropertyDescriptor PROPERTY_TRAINING_DATA = new PropertyDescriptor.Builder()
            .name("Training data")
            .description("The data used to train the model, in the format expected by Apache Open NLP tool.")
            .required(false)
            .addValidator(NON_BLANK_VALIDATOR)
            .build();

    static final PropertyDescriptor PROPERTY_TRAINING_FILE_PATH = new PropertyDescriptor.Builder()
            .name("Training file path")
            .description("Path to the training file containing data to train the model.")
            .required(false)
            .addValidator(NON_BLANK_VALIDATOR)
            .build();

    static final PropertyDescriptor PROPERTY_TRAINING_LANGUAGE = new PropertyDescriptor.Builder()
            .name("Training language")
            .description("The language code to use for detection.")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue("eng")
            .build();

    private static final PropertyDescriptor PROPERTY_CHARACTER_SET = new PropertyDescriptor.Builder()
            .name("Character Set")
            .description("The Character Set in which the content is encoded")
            .required(true)
            .addValidator(CHARACTER_SET_VALIDATOR)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .defaultValue("UTF-8")
            .build();

    private static final PropertyDescriptor PROPERTY_NLP_MODEL_STORE_PATH = new PropertyDescriptor.Builder()
            .name("Model store path")
            .description("The path where models, once trained, should bbe stored")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue("${nlp.model.store.directory}")
            .build();

    private static final PropertyDescriptor PROPERTY_TRAINING_ITERATIONS = new PropertyDescriptor.Builder()
            .name("Training iterations")
            .description("Training parameter (ITERATIONS_PARAM)")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue(String.valueOf(100))
            .build();

    private static final PropertyDescriptor PROPERTY_TRAINING_THREADS = new PropertyDescriptor.Builder()
            .name("Training threads")
            .description("Training parameter (THREADS_PARAM).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue(String.valueOf(1))
            .build();

    private static final PropertyDescriptor PROPERTY_TRAINING_TYPE = new PropertyDescriptor.Builder()
            .name("Training type")
            .description("Training parameter (TRAINER_TYPE_PARAM).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue(EventTrainer.EVENT_VALUE)
            .build();

    private static final PropertyDescriptor PROPERTY_TRAINING_VERBOSE = new PropertyDescriptor.Builder()
            .name("Training verbose")
            .description("Training parameter (VERBOSE_PARAM).")
            .required(true)
            .allowableValues("true", "false")
            .addValidator(BOOLEAN_VALIDATOR)
            .defaultValue(String.valueOf(VERBOSE_DEFAULT))
            .build();

    private final Class<M> modelClass;

    @Getter
    private final Set<Relationship> relationships = Stream.of(RELATIONSHIP_SUCCESS, RELATIONSHIP_UNMATCHED)
                                                          .collect(toSet());

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Arrays.asList(
            PROPERTY_MODEL_FILE_PATH, PROPERTY_CHARACTER_SET,
            PROPERTY_TRAINING_CUTOFF, PROPERTY_TRAINING_ITERATIONS, PROPERTY_TRAINING_ALGORITHM, PROPERTY_TRAINING_VERBOSE, PROPERTY_TRAINING_THREADS, PROPERTY_TRAINING_TYPE,
            PROPERTY_TRAINING_FILE_PATH, PROPERTY_TRAINING_DATA,
            PROPERTY_NLP_MODEL_STORE_PATH);

    AbstractNlpProcessor(Class<M> modelClass) {
        this.modelClass = modelClass;
    }

    String[] attributeAsStringArray(String value) {
        return Optional.ofNullable(value)
                       .map(s -> new Gson().fromJson(s, String[].class))
                       .orElse(new String[]{});
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        getLogger().debug("Validating {}", new Object[]{validationContext});
        Collection<ValidationResult> results = new ArrayList<>(super.customValidate(validationContext));

        final Charset charset = Charset.forName(validationContext.getProperty(PROPERTY_CHARACTER_SET).getValue());
        final TrainingParameters trainingParameters = TrainingParameters.defaultParams();

        trainingParameters.put(ITERATIONS_PARAM, validationContext.getProperty(PROPERTY_TRAINING_ITERATIONS).evaluateAttributeExpressions().asInteger());
        trainingParameters.put(CUTOFF_PARAM, validationContext.getProperty(PROPERTY_TRAINING_CUTOFF).evaluateAttributeExpressions().asInteger());
        trainingParameters.put(ALGORITHM_PARAM, validationContext.getProperty(PROPERTY_TRAINING_ALGORITHM).evaluateAttributeExpressions().getValue());
        trainingParameters.put(VERBOSE_PARAM, validationContext.getProperty(PROPERTY_TRAINING_VERBOSE).asBoolean());
        getLogger().debug("charset: {} | training : {}", new Object[]{charset, trainingParameters});

        M model = null;

        // model is loaded from file
        if (validationContext.getProperty(PROPERTY_MODEL_FILE_PATH).isSet()) {
            final String modelFilePath = validationContext.getProperty(PROPERTY_MODEL_FILE_PATH).getValue();
            getLogger().debug("modelFilePath: {}", new Object[]{modelFilePath});
            try {
                model = loadModel(modelFilePath);
            } catch (Exception e) {
                getLogger().warn("Loading model failed", e);
                results.add(new ValidationResult.Builder()
                                    .valid(false)
                                    .input(modelFilePath)
                                    .subject("Loading model failed")
                                    .explanation(e.getMessage())
                                    .build());
            }
        }

        // train model from training data file, if provided
        if (validationContext.getProperty(PROPERTY_TRAINING_FILE_PATH).isSet()) {
            final String trainingFilePath = validationContext.getProperty(PROPERTY_TRAINING_FILE_PATH).getValue();
            getLogger().debug("trainingFilePath: {}", new Object[]{trainingFilePath});
            File dataFile = get(trainingFilePath).normalize().toFile();
            try {
                model = trainModel(validationContext, results, trainingParameters, charset, new MarkableFileInputStreamFactory(dataFile));
            } catch (Exception e) {
                getLogger().warn("Training from file failed", e);
                results.add(new ValidationResult.Builder()
                                    .valid(false).input(trainingFilePath)
                                    .subject("Training from file failed")
                                    .explanation(e.getMessage()).build());
            }
        }

        // train model from training data, if provided
        if (validationContext.getProperty(PROPERTY_TRAINING_DATA).isSet()) {
            String trainingData = validationContext.getProperty(PROPERTY_TRAINING_DATA).getValue();
            getLogger().debug("trainingData: {}", new Object[]{trainingData});
            try {
                model = trainModel(validationContext, results, trainingParameters, charset, () -> IOUtils.toInputStream(trainingData, charset));
            } catch (Exception e) {
                getLogger().warn("Training from data failed", e);
                results.add(new ValidationResult.Builder()
                                    .valid(false).input(trainingData)
                                    .subject("Training from data failed")
                                    .explanation(e.getMessage()).build());
            }
        }

        boolean modelIsMissing = isTrainingRequired(validationContext) && model == null;
        if (modelIsMissing) {
            results.add(new ValidationResult.Builder()
                                .valid(false)
                                .input(String.valueOf(model))
                                .subject("Training model missing")
                                .explanation("NLP engine needs training data. Please provide a file or some data.")
                                .build());
        }

        if (model != null) {
            String modelStoreVariable = validationContext.getProperty(PROPERTY_NLP_MODEL_STORE_PATH)
                                                         .evaluateAttributeExpressions().getValue();
            getLogger().debug("modelStoreVariable: {}", new Object[]{modelStoreVariable});
            try {
                storeModel(model, modelStoreVariable);

            } catch (Exception e) {
                getLogger().warn("Writing to model store failed", e);
                results.add(new ValidationResult.Builder()
                                    .valid(false)
                                    .input(modelStoreVariable + " + " + getIdentifier() + ".bin")
                                    .subject("Cannot write to model store")
                                    .explanation("Error occurred while saving trained model: " + e.getMessage())
                                    .build());
            }
        }

        return results;
    }

    protected abstract Map<String, String> executeModel(ProcessContext context, String content, Map<String, String> attributes, M model);

    boolean isTrainingRequired(PropertyContext context) {
        return Optional.ofNullable(context).isPresent();
    }

    private M loadModel(String modelFilePath) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        getLogger().debug("loading model from: {}", new Object[]{modelFilePath});
        File modelFile = get(modelFilePath).normalize().toFile();
        return modelClass.getConstructor(File.class).newInstance(modelFile);
    }

    @OnRemoved
    public void onRemoved(ProcessContext context) {
        getLogger().info("Processor removed with context: {}", new Object[]{context});
        String modelStoreVariable = context.getProperty(PROPERTY_NLP_MODEL_STORE_PATH)
                                           .evaluateAttributeExpressions().getValue();
        getLogger().debug("modelStoreVariable: {}", new Object[]{modelStoreVariable});
        try {
            Path modelStorePath = get(modelStoreVariable);
            Path modelFilePath = modelStorePath.resolve(getIdentifier() + ".bin");
            deleteIfExists(modelFilePath);
        } catch (Exception e) {
            getLogger().error("Deleting model file failed", e);
        }
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) {
        Optional.ofNullable(session.get()).ifPresent(flowFile -> {
            final Charset charset = Charset.forName(context.getProperty(PROPERTY_CHARACTER_SET).evaluateAttributeExpressions().getValue());
            final String modelStoreVariable = context.getProperty(PROPERTY_NLP_MODEL_STORE_PATH)
                                                     .evaluateAttributeExpressions().getValue();

            AtomicReference<String> flowFileContent = new AtomicReference<>();
            session.read(flowFile, in -> flowFileContent.set(IOUtils.toString(in, charset)));

            Map<String, String> attributes = new HashMap<>(flowFile.getAttributes());
            Relationship relationship = RELATIONSHIP_UNMATCHED;

            M model = null;
            try {

                if (isTrainingRequired(context)) {
                    final Path modelStorePath = get(modelStoreVariable);
                    final Path modelFilePath = modelStorePath.resolve(getIdentifier() + ".bin");
                    model = loadModel(modelFilePath.toString());
                }

                attributes.putAll(executeModel(context, flowFileContent.get(), attributes, model));
                relationship = RELATIONSHIP_SUCCESS;

            } catch (Exception e) {
                getLogger().warn("Error while evaluating content", e);
                attributes.put(ATTRIBUTE_NLP_ERROR, e.getMessage());

            } finally {
                flowFile = session.putAllAttributes(flowFile, attributes);
                session.getProvenanceReporter().route(flowFile, relationship);
                session.transfer(flowFile, relationship);
                getLogger().info("Routing {} to {}", new Object[]{flowFile, relationship});
            }
        });
    }

    private void storeModel(M model, String modelStoreVariable) throws IOException {
        Path modelStorePath = get(modelStoreVariable);
        if (!modelStorePath.toFile().exists()) {
            createDirectory(modelStorePath);
        }
        Path modelFilePath = modelStorePath.resolve(getIdentifier() + ".bin");
        deleteIfExists(modelFilePath);
        model.serialize(modelFilePath);
    }

    protected abstract M trainModel(ValidationContext validationContext,
                                    Collection<ValidationResult> results,
                                    TrainingParameters trainingParameters,
                                    Charset charset,
                                    InputStreamFactory inputStreamFactory) throws IOException;

}
