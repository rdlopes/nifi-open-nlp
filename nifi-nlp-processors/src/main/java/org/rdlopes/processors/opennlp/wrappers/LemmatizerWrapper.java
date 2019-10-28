package org.rdlopes.processors.opennlp.wrappers;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.lemmatizer.*;
import opennlp.tools.util.*;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_SEARCH_COUNT;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_TOPK_MINIMUM_SCORE;

public class LemmatizerWrapper extends NLPToolWrapper<Lemmatizer, LemmatizerModel> {

    public LemmatizerWrapper() {
        super(LemmatizerModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, LemmatizerModel model, String content, Map<String, String> attributes) {
        final int lemmasSearchCount = LEMMATIZE_SEARCH_COUNT.getIntFrom(context);
        final double topKMinScore = LEMMATIZE_TOPK_MINIMUM_SCORE.getDoubleFrom(context);

        String[] tagsList = TAGPOS_TAG_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});
        String[] tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});

        if (tagsList.length != tokensList.length) {
            throw new IllegalArgumentException("tokens list and tags list need to be of the same length " +
                                               "(" + tagsList.length + " tags, " + tokensList.length + " tokens).");
        }

        LemmatizerME lemmatizer = new LemmatizerME(model);
        String[] results = lemmatizer.lemmatize(tokensList, tagsList);
        String[][] lemmasPrediction = lemmatizer.predictLemmas(lemmasSearchCount, tokensList, tagsList);
        String[] sesPrediction = lemmatizer.predictSES(tokensList, tagsList);
        Sequence[] topKLemmaClasses = lemmatizer.topKLemmaClasses(tokensList, tagsList, topKMinScore);
        Sequence[] topKSequences = lemmatizer.topKSequences(tokensList, tagsList, topKMinScore);
        double[] probabilities = lemmatizer.probs();

        LEMMATIZE_LEMMA_LIST.updateAttributesWithJson(attributes, results);
        LEMMATIZE_PREDICTED_LIST.updateAttributesWithJson(attributes, lemmasPrediction);
        LEMMATIZE_PREDICTED_SES_LIST.updateAttributesWithJson(attributes, sesPrediction);
        LEMMATIZE_TOPK_LIST.updateAttributesWithJson(attributes, topKLemmaClasses);
        LEMMATIZE_TOPK_SEQUENCE_LIST.updateAttributesWithJson(attributes, topKSequences);
        LEMMATIZE_PROBABILITIES.updateAttributesWithJson(attributes, probabilities);
    }

    @Override
    public LemmatizerModel trainModel(PropertyContext propertyContext,
                                      String trainingLanguage,
                                      Charset charset,
                                      TrainingParameters trainingParameters,
                                      InputStreamFactory inputStreamFactory) throws IOException {
        LemmatizerFactory factory = new LemmatizerFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<LemmaSample> sampleStream = new LemmaSampleStream(lineStream)) {
            return LemmatizerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
