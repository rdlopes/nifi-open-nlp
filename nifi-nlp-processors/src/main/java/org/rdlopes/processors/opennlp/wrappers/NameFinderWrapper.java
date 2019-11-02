package org.rdlopes.processors.opennlp.wrappers;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.namefind.*;
import opennlp.tools.util.*;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static opennlp.tools.util.Span.spansToStrings;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;

public class NameFinderWrapper extends NLPToolWrapper<TokenNameFinderModel> {

    public NameFinderWrapper() {
        super(TokenNameFinderModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, TokenNameFinderModel model, String content, Map<String, String> attributes) {
        String[] tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});

        NameFinderME nameFinder = new NameFinderME(model);
        Span[] nameSpans = nameFinder.find(tokensList);
        String[] nameList = spansToStrings(nameSpans, tokensList);
        double[] probabilities = nameFinder.probs();

        NAMEFIND_NAME_LIST.updateAttributesWithJson(attributes, nameList);
        NAMEFIND_SPAN_LIST.updateAttributesWithJson(attributes, nameSpans);
        NAMEFIND_PROBABILITIES.updateAttributesWithJson(attributes, probabilities);
    }

    @Override
    public TokenNameFinderModel trainModel(PropertyContext propertyContext,
                                           String trainingLanguage,
                                           Charset charset,
                                           TrainingParameters trainingParameters,
                                           InputStreamFactory inputStreamFactory) throws IOException {
        final String nameType = NAMEFIND_NAME_TYPE.getStringFrom(propertyContext);
        TokenNameFinderFactory factory = new TokenNameFinderFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream)) {
            return NameFinderME.train(trainingLanguage, nameType, sampleStream, trainingParameters, factory);
        }
    }
}
