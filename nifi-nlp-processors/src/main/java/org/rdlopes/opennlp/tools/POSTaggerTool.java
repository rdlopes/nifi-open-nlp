package org.rdlopes.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.postag.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.opennlp.common.NLPAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class POSTaggerTool extends NLPTool<POSModel> {
    public POSTaggerTool(Path modelPath, ComponentLog logger) {
        super(POSModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, POSModel model, Map<String, String> evaluation) {
        String[] tokensList = NLPAttribute.get(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, new TypeToken<String[]>() {
        });

        POSTagger tagger = new POSTaggerME(model);
        String[] tagsList = tagger.tag(tokensList);

        NLPAttribute.set(NLPAttribute.POS_TAGGER_TAGS_LIST_KEY, evaluation, tagsList);
    }

    @Override
    protected POSModel trainModel(ValidationContext validationContext, InputStreamFactory inputStreamFactory, TrainingParameters trainingParameters, String trainingLanguage) throws IOException {
        POSTaggerFactory factory = new POSTaggerFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream)) {
            return POSTaggerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
