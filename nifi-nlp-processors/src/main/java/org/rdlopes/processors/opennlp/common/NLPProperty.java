package org.rdlopes.processors.opennlp.common;

import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.ml.maxent.quasinewton.QNTrainer;
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;
import opennlp.tools.ml.perceptron.PerceptronTrainer;
import opennlp.tools.ml.perceptron.SimplePerceptronSequenceTrainer;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.ParserType;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.context.PropertyContext;

import static opennlp.tools.ml.AbstractTrainer.VERBOSE_DEFAULT;
import static opennlp.tools.parser.ParserType.CHUNKING;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.apache.nifi.processor.util.StandardValidators.*;
import static org.rdlopes.processors.opennlp.common.TokenizerType.SIMPLE;

public enum NLPProperty {
    COMMON_CHARACTER_SET(new PropertyDescriptor.Builder()
                                 .name("Character Set")
                                 .description("The Character Set in which the content is encoded")
                                 .expressionLanguageSupported(VARIABLE_REGISTRY)
                                 .required(true)
                                 .addValidator(CHARACTER_SET_VALIDATOR)
                                 .defaultValue("UTF-8")
                                 .build()),
    COMMON_MODELS_STORAGE_DIRECTORY(new PropertyDescriptor.Builder()
                                            .name("Models storage directory path")
                                            .description("Directory where models (either trained or loaded) are stored once validated.")
                                            .expressionLanguageSupported(VARIABLE_REGISTRY)
                                            .required(true)
                                            .addValidator(NON_BLANK_VALIDATOR)
                                            .defaultValue("${nlp.models.storage.directory}")
                                            .build()),
    TRAINED_MODEL_FILE_PATH(new PropertyDescriptor.Builder()
                                    .name("Trained model file path")
                                    .description("Path to a pre-trained NLP model file")
                                    .required(true)
                                    .expressionLanguageSupported(VARIABLE_REGISTRY)
                                    .addValidator(NON_BLANK_VALIDATOR)
                                    .build()),
    TRAINABLE_TRAINING_LANGUAGE(new PropertyDescriptor.Builder()
                                        .name("Training language")
                                        .description("The language code to use for detection.")
                                        .required(true)
                                        .expressionLanguageSupported(VARIABLE_REGISTRY)
                                        .addValidator(NON_BLANK_VALIDATOR)
                                        .defaultValue("eng")
                                        .build()),
    TRAINABLE_TRAINING_DATA(new PropertyDescriptor.Builder()
                                    .name("Training data")
                                    .description("The data used to train the model, in the format expected by Apache Open NLP tool.")
                                    .required(false)
                                    .addValidator(NON_BLANK_VALIDATOR)
                                    .build()),
    TRAINABLE_TRAINING_FILE_PATH(new PropertyDescriptor.Builder()
                                         .name("Training data file path")
                                         .description("Path to the training file containing data to train the model.")
                                         .required(false)
                                         .addValidator(NON_BLANK_VALIDATOR)
                                         .build()),
    TRAINABLE_TRAINING_PARAM_ALGORITHM(new PropertyDescriptor.Builder()
                                               .name("Training algorithm")
                                               .description("ALGORITHM_PARAM parameter")
                                               .required(true)
                                               .allowableValues(
                                                       new AllowableValue(GISTrainer.MAXENT_VALUE, "GIS / maximum entropy"),
                                                       new AllowableValue(PerceptronTrainer.PERCEPTRON_VALUE, "ML / perceptron"),
                                                       new AllowableValue(SimplePerceptronSequenceTrainer.PERCEPTRON_SEQUENCE_VALUE, "ML / perceptron sequence"),
                                                       new AllowableValue(QNTrainer.MAXENT_QN_VALUE, "QN / maximum entropy"),
                                                       new AllowableValue(NaiveBayesTrainer.NAIVE_BAYES_VALUE, "Naive Bayes / classifier")
                                               )
                                               .addValidator(NON_BLANK_VALIDATOR)
                                               .defaultValue(GISTrainer.MAXENT_VALUE)
                                               .build()),
    TRAINABLE_TRAINING_PARAM_CUTOFF(new PropertyDescriptor.Builder()
                                            .name("Training cut off")
                                            .description("CUTOFF_PARAM parameter")
                                            .required(true)
                                            .expressionLanguageSupported(VARIABLE_REGISTRY)
                                            .addValidator(INTEGER_VALIDATOR)
                                            .defaultValue(String.valueOf(5))
                                            .build()),
    TRAINABLE_TRAINING_PARAM_ITERATIONS(new PropertyDescriptor.Builder()
                                                .name("Training iterations")
                                                .description("ITERATIONS_PARAM parameter")
                                                .required(true)
                                                .expressionLanguageSupported(VARIABLE_REGISTRY)
                                                .addValidator(INTEGER_VALIDATOR)
                                                .defaultValue(String.valueOf(100))
                                                .build()),
    TRAINABLE_TRAINING_PARAM_THREADS(new PropertyDescriptor.Builder()
                                             .name("Training threads")
                                             .description("THREADS_PARAM parameter")
                                             .required(true)
                                             .expressionLanguageSupported(VARIABLE_REGISTRY)
                                             .addValidator(INTEGER_VALIDATOR)
                                             .defaultValue(String.valueOf(1))
                                             .build()),
    TRAINABLE_TRAINING_PARAM_TYPE(new PropertyDescriptor.Builder()
                                          .name("Training type")
                                          .description("TRAINER_TYPE_PARAM parameter")
                                          .required(true)
                                          .expressionLanguageSupported(VARIABLE_REGISTRY)
                                          .addValidator(NON_BLANK_VALIDATOR)
                                          .defaultValue(EventTrainer.EVENT_VALUE)
                                          .build()),
    TRAINABLE_TRAINING_PARAM_VERBOSE(new PropertyDescriptor.Builder()
                                             .name("Training verbose")
                                             .description("VERBOSE_PARAM parameter")
                                             .required(true)
                                             .allowableValues("true", "false")
                                             .addValidator(BOOLEAN_VALIDATOR)
                                             .defaultValue(String.valueOf(VERBOSE_DEFAULT))
                                             .build()),
    LEMMATIZE_SEARCH_COUNT(new PropertyDescriptor.Builder()
                                   .name("Lemmas search count")
                                   .description("When predicting lemmas, how many lemmas do you want to search for.")
                                   .required(true)
                                   .expressionLanguageSupported(VARIABLE_REGISTRY)
                                   .addValidator(INTEGER_VALIDATOR)
                                   .defaultValue("1")
                                   .build()),
    LEMMATIZE_TOPK_MINIMUM_SCORE(new PropertyDescriptor.Builder()
                                         .name("Top K search minimum probability")
                                         .description("When predicting classes, engine will discard any class which confidence is below the given double value.")
                                         .required(true)
                                         .expressionLanguageSupported(VARIABLE_REGISTRY)
                                         .addValidator(NUMBER_VALIDATOR)
                                         .defaultValue("0.0")
                                         .build()),
    NAMEFIND_NAME_TYPE(new PropertyDescriptor.Builder()
                               .name("Name type")
                               .description("The name type to look for (might depend on the model selected).")
                               .required(true)
                               .expressionLanguageSupported(VARIABLE_REGISTRY)
                               .addValidator(NON_BLANK_VALIDATOR)
                               .defaultValue("person")
                               .build()),
    PARSER_PARSES_COUNT(new PropertyDescriptor.Builder()
                                .name("Top parses list size")
                                .description("The number of parses that the tool should evaluate (1-10).")
                                .required(true)
                                .expressionLanguageSupported(VARIABLE_REGISTRY)
                                .addValidator(INTEGER_VALIDATOR)
                                .defaultValue("1")
                                .build()),
    PARSER_ADVANCE_PERCENTAGE(new PropertyDescriptor.Builder()
                                      .name("Advance percentage")
                                      .description("Advance percentage for parser setup, as a float number between 0 and 1.")
                                      .required(true)
                                      .expressionLanguageSupported(VARIABLE_REGISTRY)
                                      .addValidator(NUMBER_VALIDATOR)
                                      .defaultValue(String.valueOf(AbstractBottomUpParser.defaultAdvancePercentage))
                                      .build()),
    PARSER_BEAM_SIZE(new PropertyDescriptor.Builder()
                             .name("Beam size")
                             .description("Beam size for parser setup, as an integer.")
                             .required(true)
                             .expressionLanguageSupported(VARIABLE_REGISTRY)
                             .addValidator(INTEGER_VALIDATOR)
                             .defaultValue(String.valueOf(AbstractBottomUpParser.defaultBeamSize))
                             .build()),
    PARSER_HEAD_RULES_FILE_PATH(new PropertyDescriptor.Builder()
                                        .name("Head rules file path")
                                        .description("Head rules file path for training the model " +
                                                     "(only required if training data or training file are set).")
                                        .required(false)
                                        .expressionLanguageSupported(VARIABLE_REGISTRY)
                                        .addValidator(NON_BLANK_VALIDATOR)
                                        .defaultValue("${NIFI_HOME}/models/en-head_rules")
                                        .build()),
    PARSER_PARSER_TYPE(new PropertyDescriptor.Builder()
                               .name("Parser type")
                               .description("The type of parser to use.")
                               .required(true)
                               .allowableValues(ParserType.values())
                               .defaultValue(CHUNKING.name())
                               .build()),
    TOKENIZE_TOKENIZER_TYPE(new PropertyDescriptor.Builder()
                                    .name("Tokenizer type")
                                    .description("Defines the tokenizer implementation to use, as defined by Apache NLP." +
                                                 "Most part-of-speech taggers, parsers and so on, work with text tokenized in this manner. " +
                                                 "It is important to ensure that your tokenizer produces tokens of the type expected by your later text processing components.")
                                    .required(true)
                                    .allowableValues("WHITESPACE", "SIMPLE")
                                    .addValidator(NON_BLANK_VALIDATOR)
                                    .defaultValue(SIMPLE.name())
                                    .build());

    public final PropertyDescriptor descriptor;

    NLPProperty(PropertyDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public double getDoubleFrom(PropertyContext propertyContext) {
        return getValueFrom(propertyContext).asDouble();
    }

    public <E extends Enum<E>> E getEnumFrom(PropertyContext propertyContext, Class<E> parserTypeClass) {
        return Enum.valueOf(parserTypeClass, getStringFrom(propertyContext));
    }

    public int getIntFrom(PropertyContext propertyContext) {
        return getValueFrom(propertyContext).asInteger();
    }

    public String getStringFrom(PropertyContext propertyContext) {
        return getValueFrom(propertyContext).getValue();
    }

    private PropertyValue getValueFrom(PropertyContext propertyContext) {
        return descriptor.isExpressionLanguageSupported()
               ? propertyContext.getProperty(descriptor)
                                .evaluateAttributeExpressions()
               : propertyContext.getProperty(descriptor);
    }

    public boolean isSetIn(PropertyContext propertyContext) {
        return propertyContext.getProperty(descriptor).isSet();
    }
}
