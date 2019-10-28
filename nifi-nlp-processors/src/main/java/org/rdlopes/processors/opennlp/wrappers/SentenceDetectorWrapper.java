package org.rdlopes.processors.opennlp.wrappers;

import opennlp.tools.sentdetect.*;
import opennlp.tools.util.*;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.processors.opennlp.common.NLPAttribute;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class SentenceDetectorWrapper extends NLPToolWrapper<SentenceDetector, SentenceModel> {

    public SentenceDetectorWrapper() {
        super(SentenceModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, SentenceModel model, String content, Map<String, String> attributes) {
        SentenceDetectorME detector = new SentenceDetectorME(model);

        String[] chunks = detector.sentDetect(content);
        Span[] chunkAsSpans = detector.sentPosDetect(content);
        double[] probabilities = detector.getSentenceProbabilities();

        NLPAttribute.SENTDET_CHUNK_LIST.updateAttributesWithJson(attributes, chunks);
        NLPAttribute.SENTDET_SPAN_LIST.updateAttributesWithJson(attributes, chunkAsSpans);
        NLPAttribute.SENTDET_PROBABILITIES.updateAttributesWithJson(attributes, probabilities);
    }

    @Override
    public SentenceModel trainModel(PropertyContext propertyContext,
                                    String trainingLanguage,
                                    Charset charset,
                                    TrainingParameters trainingParameters,
                                    InputStreamFactory inputStreamFactory) throws IOException {
        SentenceDetectorFactory factory = new SentenceDetectorFactory(trainingLanguage, true, null, null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream)) {
            return SentenceDetectorME.train(trainingLanguage, sampleStream, factory, trainingParameters);
        }
    }
}
