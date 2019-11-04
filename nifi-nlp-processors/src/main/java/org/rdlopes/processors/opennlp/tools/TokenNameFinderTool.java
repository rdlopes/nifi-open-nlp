package org.rdlopes.processors.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.namefind.*;
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
import static opennlp.tools.util.Span.spansToStrings;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;

public class TokenNameFinderTool extends NLPTool<TokenNameFinderModel> {
    public TokenNameFinderTool(Path modelPath, ComponentLog logger) {
        super(TokenNameFinderModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, TokenNameFinderModel model, Map<String, String> evaluation) {
        String[] tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});

        TokenNameFinder nameFinder = new NameFinderME(model);
        Span[] nameSpans = nameFinder.find(tokensList);
        String[] nameList = spansToStrings(nameSpans, tokensList);

        NAMEFIND_NAME_LIST.updateAttributesWithJson(attributes, nameList);
        NAMEFIND_SPAN_LIST.updateAttributesWithJson(attributes, nameSpans);
    }

    @Override
    protected TokenNameFinderModel trainModel(ValidationContext validationContext,
                                              InputStreamFactory inputStreamFactory,
                                              TrainingParameters trainingParameters,
                                              String trainingLanguage) throws IOException {
        final String nameType = NAMEFIND_NAME_TYPE.getStringFrom(validationContext);
        TokenNameFinderFactory factory = new TokenNameFinderFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream)) {
            return NameFinderME.train(trainingLanguage, nameType, sampleStream, trainingParameters, factory);
        }
    }
}
