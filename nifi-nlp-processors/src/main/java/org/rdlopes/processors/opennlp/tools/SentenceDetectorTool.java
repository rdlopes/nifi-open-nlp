package org.rdlopes.processors.opennlp.tools;

import opennlp.tools.sentdetect.*;
import opennlp.tools.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.processors.opennlp.common.NLPAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SentenceDetectorTool extends NLPTool<SentenceModel> {
    public SentenceDetectorTool(Path modelPath, ComponentLog logger) {
        super(SentenceModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, SentenceModel model, Map<String, String> evaluation)
            throws IOException {
        SentenceDetector detector = new SentenceDetectorME(model);
        String contentString = IOUtils.toString(content, charset);

        String[] chunks = detector.sentDetect(contentString);
        Span[] chunkAsSpans = detector.sentPosDetect(contentString);

        NLPAttribute.SENTDET_CHUNK_LIST.updateAttributesWithJson(attributes, chunks);
        NLPAttribute.SENTDET_SPAN_LIST.updateAttributesWithJson(attributes, chunkAsSpans);
    }

    @Override
    protected SentenceModel trainModel(ValidationContext validationContext, InputStreamFactory inputStreamFactory, TrainingParameters trainingParameters, String trainingLanguage) throws IOException {
        SentenceDetectorFactory factory = new SentenceDetectorFactory(trainingLanguage, true, null, null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream)) {
            return SentenceDetectorME.train(trainingLanguage, sampleStream, factory, trainingParameters);
        }
    }
}
