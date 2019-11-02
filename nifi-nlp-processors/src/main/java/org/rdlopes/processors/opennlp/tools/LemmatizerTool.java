package org.rdlopes.processors.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.lemmatizer.*;
import opennlp.tools.util.*;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_SEARCH_COUNT;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_TOPK_MINIMUM_SCORE;

public class LemmatizerTool extends NLPTool<LemmatizerModel> {
    public LemmatizerTool(Path modelPath, ComponentLog logger) {
        super(LemmatizerModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, LemmatizerModel model, Map<String, String> evaluation) {
        final int lemmasSearchCount = LEMMATIZE_SEARCH_COUNT.getIntFrom(processContext);
        final double topKMinScore = LEMMATIZE_TOPK_MINIMUM_SCORE.getDoubleFrom(processContext);

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
    protected LemmatizerModel trainModel(ValidationContext validationContext,
                                         InputStreamFactory inputStreamFactory,
                                         TrainingParameters trainingParameters,
                                         String trainingLanguage) throws IOException {
        LemmatizerFactory factory = new LemmatizerFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ObjectStream<LemmaSample> sampleStream = new LemmaSampleStream(lineStream)) {
            return LemmatizerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
