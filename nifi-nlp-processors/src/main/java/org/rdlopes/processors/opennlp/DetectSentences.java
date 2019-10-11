package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.sentdetect.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR_DESCRIPTION;
import static org.rdlopes.processors.opennlp.DetectSentences.*;

@NlpProcessor
@Tags({"apache", "nlp", "sentence", "detection"})
@CapabilityDescription("Detects the sentences in the content of a flow file.")
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR,
                                    description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_CHUNK_COUNT,
                                    description = "Holds the sentence chunks count found in the flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_CHUNK_LIST,
                                    description = ATTRIBUTE_SENTDET_CHUNK_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_CHUNK_SPANS,
                                    description = "Holds the sentence chunks list found in the flow file content, " +
                                                  "as a JSON spans list."),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_SENTENCE_PROBABILITIES,
                                    description = "Holds the probability for each sentence prediction, " +
                                                  "as a JSON double list.")})
@EqualsAndHashCode(callSuper = true)
public class DetectSentences extends AbstractNlpProcessor<SentenceModel> {

    public static final String ATTRIBUTE_SENTDET_CHUNK_COUNT = "nlp.sentdet.chunk.count";

    public static final String ATTRIBUTE_SENTDET_CHUNK_LIST = "nlp.sentdet.chunk.list";

    public static final String ATTRIBUTE_SENTDET_CHUNK_LIST_DESCRIPTION = "Holds the sentence chunks list found in the flow file content.";

    public static final String ATTRIBUTE_SENTDET_CHUNK_SPANS = "nlp.sentdet.chunk.spans";

    public static final String ATTRIBUTE_SENTDET_SENTENCE_PROBABILITIES = "nlp.sentdet.sentence.probabilities";

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(Stream.of(PROPERTY_TRAINING_LANGUAGE),
                                                                                        super.getSupportedPropertyDescriptors().stream())
                                                                                .collect(toList());

    public DetectSentences() {super(SentenceModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, ProcessSession session, String content, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();
        SentenceDetectorME detector = new SentenceDetectorME(getModel());

        String[] chunks = detector.sentDetect(content);
        Span[] chunkAsSpans = detector.sentPosDetect(content);
        double[] probabilities = detector.getSentenceProbabilities();

        evaluation.put(ATTRIBUTE_SENTDET_CHUNK_COUNT, String.valueOf(chunks.length));
        evaluation.put(ATTRIBUTE_SENTDET_CHUNK_LIST, new Gson().toJson(chunks));
        evaluation.put(ATTRIBUTE_SENTDET_CHUNK_SPANS, new Gson().toJson(chunkAsSpans));
        evaluation.put(ATTRIBUTE_SENTDET_SENTENCE_PROBABILITIES, new Gson().toJson(probabilities));

        return evaluation;
    }

    @Override
    protected SentenceModel doTrain(ValidationContext context, TrainingParameters parameters, Charset charset, ObjectStream<String> stream) throws IOException {
        final String trainingLanguage = context.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        SentenceDetectorFactory factory = new SentenceDetectorFactory();
        try (ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(stream)) {
            return SentenceDetectorME.train(trainingLanguage, sampleStream, factory, parameters);
        }
    }
}
