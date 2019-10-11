package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.READ;
import static java.util.stream.Collectors.toSet;
import static opennlp.tools.ml.AbstractTrainer.VERBOSE_DEFAULT;
import static opennlp.tools.ml.AbstractTrainer.VERBOSE_PARAM;
import static opennlp.tools.util.TrainingParameters.*;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.apache.nifi.processor.util.StandardValidators.*;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractNlpProcessor<M extends BaseModel> extends AbstractProcessor {

    public static final String ATTRIBUTE_NLP_ERROR = "nlp.error";

    public static final String ATTRIBUTE_NLP_ERROR_DESCRIPTION = "Error message raised by processing the content, if any.";

    public static final PropertyDescriptor PROPERTY_CHARACTER_SET = new PropertyDescriptor.Builder()
            .name("Character Set")
            .description("The Character Set in which the content is encoded")
            .required(true)
            .addValidator(CHARACTER_SET_VALIDATOR)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .defaultValue("UTF-8")
            .build();

    public static final PropertyDescriptor PROPERTY_MODEL_FILE_PATH = new PropertyDescriptor.Builder()
            .name("Training model")
            .description("Path to the model for the NLP engine.")
            .required(false)
            .addValidator(NON_BLANK_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROPERTY_MODEL_STORE_PATH = new PropertyDescriptor.Builder()
            .name("Model store path")
            .description("Path to the directory where all training models will be stored.")
            .required(true)
            .defaultValue("${nlp.model.store.directory}")
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_ALGORITHM = new PropertyDescriptor.Builder()
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

    public static final PropertyDescriptor PROPERTY_TRAINING_CUTOFF = new PropertyDescriptor.Builder()
            .name("Training cut off")
            .description("Training parameter (CUTOFF_PARAM).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue(String.valueOf(5))
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_DATA = new PropertyDescriptor.Builder()
            .name("Training data")
            .description("The data used to train the model, in the format expected by Apache Open NLP tool.")
            .required(false)
            .addValidator(NON_BLANK_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_FILE_PATH = new PropertyDescriptor.Builder()
            .name("Training file path")
            .description("Path to the training file containing data to train the model.")
            .required(false)
            .addValidator(NON_BLANK_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_ITERATIONS = new PropertyDescriptor.Builder()
            .name("Training iterations")
            .description("Training parameter (ITERATIONS_PARAM)")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue(String.valueOf(100))
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_LANGUAGE = new PropertyDescriptor.Builder()
            .name("Training language")
            .description("The language code to use for detection.")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue("en")
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_THREADS = new PropertyDescriptor.Builder()
            .name("Training threads")
            .description("Training parameter (THREADS_PARAM).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue(String.valueOf(1))
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_TYPE = new PropertyDescriptor.Builder()
            .name("Training type")
            .description("Training parameter (TRAINER_TYPE_PARAM).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue(EventTrainer.EVENT_VALUE)
            .build();

    public static final PropertyDescriptor PROPERTY_TRAINING_VERBOSE = new PropertyDescriptor.Builder()
            .name("Training verbose")
            .description("Training parameter (VERBOSE_PARAM).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .allowableValues("true", "false")
            .addValidator(BOOLEAN_VALIDATOR)
            .defaultValue(String.valueOf(VERBOSE_DEFAULT))
            .build();

    public static final Relationship RELATIONSHIP_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Parsing completed successfully")
            .build();

    public static final Relationship RELATIONSHIP_UNMATCHED = new Relationship.Builder()
            .name("unmatched")
            .description("Unmatched content")
            .build();

    private final Class<M> modelClass;

    @Getter
    private final Set<Relationship> relationships = Stream.of(RELATIONSHIP_SUCCESS, RELATIONSHIP_UNMATCHED)
                                                          .collect(toSet());

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Arrays.asList(
            PROPERTY_MODEL_FILE_PATH, PROPERTY_MODEL_STORE_PATH, PROPERTY_CHARACTER_SET,
            PROPERTY_TRAINING_CUTOFF, PROPERTY_TRAINING_ITERATIONS, PROPERTY_TRAINING_ALGORITHM, PROPERTY_TRAINING_VERBOSE, PROPERTY_TRAINING_THREADS, PROPERTY_TRAINING_TYPE,
            PROPERTY_TRAINING_FILE_PATH, PROPERTY_TRAINING_DATA);

    protected AbstractNlpProcessor(Class<M> modelClass) {this.modelClass = modelClass;}

    String[] attributeAsStringArray(String value) {
        return Optional.ofNullable(value)
                       .map(s -> new Gson().fromJson(s, String[].class))
                       .orElse(new String[]{});
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        Collection<ValidationResult> results = new ArrayList<>(super.customValidate(validationContext));

        final Charset charset = Charset.forName(validationContext.getProperty(PROPERTY_CHARACTER_SET).getValue());
        final TrainingParameters trainingParameters = TrainingParameters.defaultParams();

        trainingParameters.put(ITERATIONS_PARAM, validationContext.getProperty(PROPERTY_TRAINING_ITERATIONS).evaluateAttributeExpressions().asInteger());
        trainingParameters.put(CUTOFF_PARAM, validationContext.getProperty(PROPERTY_TRAINING_CUTOFF).evaluateAttributeExpressions().asInteger());
        trainingParameters.put(ALGORITHM_PARAM, validationContext.getProperty(PROPERTY_TRAINING_ALGORITHM).evaluateAttributeExpressions().getValue());
        trainingParameters.put(VERBOSE_PARAM, validationContext.getProperty(PROPERTY_TRAINING_VERBOSE).asBoolean());
        trainingParameters.put(TRAINER_TYPE_PARAM, validationContext.getProperty(PROPERTY_TRAINING_TYPE).getValue());
        trainingParameters.put(THREADS_PARAM, validationContext.getProperty(PROPERTY_TRAINING_THREADS).asInteger());

        M trainingModel = null;

        // train model from input file, if provided
        if (validationContext.getProperty(PROPERTY_MODEL_FILE_PATH).isSet()) {
            final String modelFilePath = validationContext.getProperty(PROPERTY_MODEL_FILE_PATH).getValue();
            getLogger().debug("PROPERTY_MODEL_FILE_PATH is set: {} - loading model from file", new Object[]{modelFilePath});

            try {
                File modelFile = Paths.get(modelFilePath).normalize().toFile();
                trainingModel = modelClass.getConstructor(File.class)
                                          .newInstance(modelFile);

            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                getLogger().warn("Validation failed / Loading model file {} raised an error", new Object[]{modelFilePath}, e);
                results.add(new ValidationResult.Builder()
                                    .valid(false)
                                    .input(modelFilePath)
                                    .subject("NLP engine training failed")
                                    .explanation("Loading model file " + modelFilePath + " raised an error: " + e.getMessage())
                                    .build());
            }
        }


        // train model from data file, if provided
        if (validationContext.getProperty(PROPERTY_TRAINING_FILE_PATH).isSet()) {
            final String trainingFilePath = validationContext.getProperty(PROPERTY_TRAINING_FILE_PATH).getValue();
            File trainingFile = Paths.get(trainingFilePath).normalize().toFile();
            getLogger().debug("PROPERTY_TRAINING_FILE_PATH is set - loading training file: {}", new Object[]{trainingFile});
            try (ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(trainingFile), charset)) {
                trainingModel = doTrain(validationContext, trainingParameters, charset, lineStream);
            } catch (IOException e) {
                getLogger().warn("Validation failed / Training from data file {} raised an error", new Object[]{trainingFile}, e);
                results.add(new ValidationResult.Builder()
                                    .valid(false)
                                    .input(trainingFile.getAbsolutePath())
                                    .subject("NLP engine training failed")
                                    .explanation("Training from data file " + trainingFile + " raised an error: " + e.getMessage())
                                    .build());
            }
        }

        // train model from training data, if provided
        if (validationContext.getProperty(PROPERTY_TRAINING_DATA).isSet()) {
            String trainingData = validationContext.getProperty(PROPERTY_TRAINING_DATA).getValue();
            getLogger().debug("PROPERTY_TRAINING_DATA is set - loading training data: {}", new Object[]{trainingData});
            try (ObjectStream<String> lineStream = new PlainTextByLineStream(() -> toInputStream(trainingData, charset), charset)) {
                trainingModel = doTrain(validationContext, trainingParameters, charset, lineStream);
            } catch (IOException e) {
                getLogger().warn("Validation failed / Training from data raised an error - data: {}", new Object[]{trainingData}, e);
                results.add(new ValidationResult.Builder()
                                    .valid(false)
                                    .input(trainingData)
                                    .subject("NLP engine training failed")
                                    .explanation("Training from data failed: " + e.getMessage() + " with data " + trainingData)
                                    .build());
            }
        }

        // we do need a training model
        if (isTrainingRequired(validationContext) && trainingModel == null) {
            getLogger().warn("Validation failed / Missing training model");
            results.add(new ValidationResult.Builder()
                                .valid(false)
                                .input("null")
                                .subject("Training model missing")
                                .explanation("NLP engine needs a training model. Please provide a file or some data to train.")
                                .build());
        }

        Optional.ofNullable(trainingModel)
                .ifPresent(model -> {
                    String modelStoreDirectory = validationContext.getProperty(PROPERTY_MODEL_STORE_PATH)
                                                                  .evaluateAttributeExpressions()
                                                                  .getValue();
                    Path modelFilePath = Paths.get(modelStoreDirectory)
                                              .resolve(getIdentifier());
                    try {
                        model.serialize(modelFilePath);
                    } catch (IOException e) {
                        getLogger().warn("Validation failed / Storing training model at path {} raised an error", new Object[]{modelFilePath}, e);
                        results.add(new ValidationResult.Builder()
                                            .valid(false)
                                            .input(modelFilePath.toString())
                                            .subject("Storing training model failed")
                                            .explanation("Storing training model at path " + modelFilePath + " raised an error: " + e.getMessage())
                                            .build());
                    }
                });

        return results;
    }

    protected abstract Map<String, String> doEvaluate(ProcessContext context, M trainingModel, String content, Map<String, String> attributes) throws IOException;

    protected abstract M doTrain(ValidationContext context, TrainingParameters parameters, Charset charset, ObjectStream<String> stream) throws IOException;

    protected boolean isTrainingRequired(PropertyContext context) {
        return Optional.ofNullable(context).isPresent();
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        Optional.ofNullable(session.get()).ifPresent(flowFile -> {

            // load content from flow file
            final Charset charset = Charset.forName(context.getProperty(PROPERTY_CHARACTER_SET).evaluateAttributeExpressions().getValue());
            AtomicReference<String> flowFileContent = new AtomicReference<>();
            session.read(flowFile, in -> flowFileContent.set(IOUtils.toString(in, charset)));

            Map<String, String> attributes = new HashMap<>(flowFile.getAttributes());
            Relationship relationship = RELATIONSHIP_UNMATCHED;

            String modelStorePath = context.getProperty(PROPERTY_MODEL_STORE_PATH).evaluateAttributeExpressions().getValue();
            try (InputStream trainingModelInputStream = Files.newInputStream(Paths.get(modelStorePath), READ)) {
                M trainingModel = modelClass.getConstructor(InputStream.class)
                                            .newInstance(trainingModelInputStream);
                attributes.putAll(doEvaluate(context, trainingModel, flowFileContent.get(), attributes));
                relationship = RELATIONSHIP_SUCCESS;

            } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
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

}
