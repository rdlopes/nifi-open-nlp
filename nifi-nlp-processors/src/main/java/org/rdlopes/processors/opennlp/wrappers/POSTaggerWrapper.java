package org.rdlopes.processors.opennlp.wrappers;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.postag.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static org.rdlopes.processors.opennlp.common.NLPAttribute.TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;

public class POSTaggerWrapper extends NLPToolWrapper<POSTagger, POSModel> {

    public POSTaggerWrapper() {
        super(POSModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, POSModel model, String content, Map<String, String> attributes) {
        String[] tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});

        POSTagger tagger = new POSTaggerME(model);
        String[] tagsList = tagger.tag(tokensList);

        TAGPOS_TAG_LIST.updateAttributesWithJson(attributes, tagsList);
    }

    @Override
    public POSModel trainModel(PropertyContext propertyContext, String trainingLanguage, Charset charset, TrainingParameters trainingParameters, InputStreamFactory inputStreamFactory)
            throws IOException {
        POSTaggerFactory factory = new POSTaggerFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream)) {
            return POSTaggerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
