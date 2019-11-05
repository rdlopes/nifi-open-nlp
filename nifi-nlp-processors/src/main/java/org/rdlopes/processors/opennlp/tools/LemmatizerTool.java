package org.rdlopes.processors.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.lemmatizer.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
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

public class LemmatizerTool extends NLPTool<LemmatizerModel> {
    public LemmatizerTool(Path modelPath, ComponentLog logger) {
        super(LemmatizerModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, LemmatizerModel model, Map<String, String> evaluation) {
        String[] tagsList = get(POS_TAGGER_TAGS_LIST_KEY, attributes, new TypeToken<String[]>() {});
        String[] tokensList = get(TOKENIZER_TOKENS_LIST_KEY, attributes, new TypeToken<String[]>() {});

        if (tagsList.length != tokensList.length) {
            throw new IllegalArgumentException("tokens list and tags list need to be of the same length " +
                                               "(" + tagsList.length + " tags, " + tokensList.length + " tokens).");
        }

        Lemmatizer lemmatizer = new LemmatizerME(model);
        String[] results = lemmatizer.lemmatize(tokensList, tagsList);

        set(LEMMATIZER_LEMMAS_LIST_KEY, evaluation, results);
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
