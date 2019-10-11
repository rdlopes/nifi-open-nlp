package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.lemmatizer.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.apache.nifi.processor.util.StandardValidators.INTEGER_VALIDATOR;
import static org.apache.nifi.processor.util.StandardValidators.NUMBER_VALIDATOR;
import static org.rdlopes.processors.opennlp.Lemmatize.*;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION;

@NlpProcessor
@Tags({"apache", "nlp", "lemmatizer"})
@CapabilityDescription("Lemmatizes the content of a flow file.")
@ReadsAttributes({@ReadsAttribute(attribute = ATTRIBUTE_TAGPOS_TAG_LIST, description = ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION),
                  @ReadsAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_LIST, description = ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_COUNT, description = "Lemmas list size."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_LIST, description = "Lemmas list as evaluated from flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_COUNT, description = "Lemmas predicted count"),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_LIST, description = "List of predicted lemmas."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_COUNT, description = "SES prediction list size."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_LIST, description = "SES prediction list"),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_COUNT, description = "Top K lemmas list size."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_LIST, description = "Top K lemmas list."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_COUNT, description = "Top K sequence list size."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_LIST, description = "Top K sequence list, as a JSON list of sequence objects."),
                   @WritesAttribute(attribute = ATTRIBUTE_LEMMATIZE_LEMMA_PROBABILITIES, description = "Lemmas probabilities, as a JSON list of doubles.")})
@EqualsAndHashCode(callSuper = true)
public class Lemmatize extends AbstractNlpProcessor<LemmatizerModel> {

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_COUNT = "nlp.lemmatize.lemma.count";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_LIST = "nlp.lemmatize.lemma.list";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_COUNT = "nlp.lemmatize.lemma.predicted.count";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_LIST = "nlp.lemmatize.lemma.predicted.list";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_COUNT = "nlp.lemmatize.lemma.predicted.ses.count";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_LIST = "nlp.lemmatize.lemma.predicted.ses.list";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_PROBABILITIES = "nlp.lemmatize.lemma.probabilities";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_COUNT = "nlp.lemmatize.lemma.topk.lemma.count";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_LIST = "nlp.lemmatize.lemma.topk.lemma.list";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_COUNT = "nlp.lemmatize.lemma.topk.sequence.count";

    static final String ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_LIST = "nlp.lemmatize.lemma.topk.sequence.list";

    private static final PropertyDescriptor PROPERTY_LEMMAS_SEARCH_COUNT = new PropertyDescriptor.Builder()
            .name("Lemmas search count")
            .description("When predicting lemmas, how many lemmas do you want to search for.")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue("1")
            .build();

    private static final PropertyDescriptor PROPERTY_TOPK_MINIMUM_SCORE = new PropertyDescriptor.Builder()
            .name("Top K search minimum probability")
            .description("When predicting classes, engine will discard any class which confidence is below the given double value.")
            .required(false)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NUMBER_VALIDATOR)
            .build();

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream
            .concat(Stream.of(PROPERTY_TRAINING_LANGUAGE, PROPERTY_LEMMAS_SEARCH_COUNT, PROPERTY_TOPK_MINIMUM_SCORE),
                    super.getSupportedPropertyDescriptors().stream())
            .collect(toList());

    public Lemmatize() {super(LemmatizerModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, String content, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();
        final int lemmasSearchCount = context.getProperty(PROPERTY_LEMMAS_SEARCH_COUNT).evaluateAttributeExpressions().asInteger();
        String[] tagsList = attributeAsStringArray(attributes.get(ATTRIBUTE_TAGPOS_TAG_LIST));
        String[] tokensList = attributeAsStringArray(attributes.get(ATTRIBUTE_TOKENIZE_TOKEN_LIST));

        if (tagsList.length != tokensList.length) {
            throw new IllegalArgumentException("tokens list and tags list need to be of the same length " +
                                               "(" + tagsList.length + " tags, " + tokensList.length + " tokens).");
        }

        LemmatizerME lemmatizer = new LemmatizerME(getModel());
        String[] results = lemmatizer.lemmatize(tokensList, tagsList);
        String[][] lemmasPrediction = lemmatizer.predictLemmas(lemmasSearchCount, tokensList, tagsList);
        String[] sesPrediction = lemmatizer.predictSES(tokensList, tagsList);
        Sequence[] topKLemmaClasses = context.getProperty(PROPERTY_TOPK_MINIMUM_SCORE).isSet() ?
                                      lemmatizer.topKLemmaClasses(tokensList, tagsList, context.getProperty(PROPERTY_TOPK_MINIMUM_SCORE)
                                                                                               .evaluateAttributeExpressions()
                                                                                               .asDouble()) :
                                      lemmatizer.topKLemmaClasses(tokensList, tagsList);
        Sequence[] topKSequences = context.getProperty(PROPERTY_TOPK_MINIMUM_SCORE).isSet() ?
                                   lemmatizer.topKSequences(tokensList, tagsList, context.getProperty(PROPERTY_TOPK_MINIMUM_SCORE)
                                                                                         .evaluateAttributeExpressions()
                                                                                         .asDouble())
                                                                                            : lemmatizer.topKSequences(tokensList, tagsList);
        double[] probabilities = lemmatizer.probs();
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_COUNT, String.valueOf(results.length));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_LIST, new Gson().toJson(results));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_COUNT, String.valueOf(lemmasPrediction.length));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_LIST, new Gson().toJson(lemmasPrediction));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_COUNT, String.valueOf(sesPrediction.length));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_LIST, new Gson().toJson(sesPrediction));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_COUNT, String.valueOf(topKLemmaClasses.length));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_LIST, new Gson().toJson(topKLemmaClasses));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_COUNT, String.valueOf(topKSequences.length));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_LIST, new Gson().toJson(topKSequences));
        evaluation.put(ATTRIBUTE_LEMMATIZE_LEMMA_PROBABILITIES, new Gson().toJson(probabilities));
        return evaluation;
    }

    @Override
    protected LemmatizerModel doTrain(ValidationContext context, TrainingParameters parameters, Charset charset, ObjectStream<String> stream) throws IOException {
        final String trainingLanguage = context.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        LemmatizerFactory factory = new LemmatizerFactory();
        try (ObjectStream<LemmaSample> sampleStream = new LemmaSampleStream(stream)) {
            return LemmatizerME.train(trainingLanguage, sampleStream, parameters, factory);
        }
    }
}
