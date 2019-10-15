package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.sentdetect.*;
import opennlp.tools.util.*;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
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
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_CHUNK_COUNT, description = "Holds the sentence chunks count found in the flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_CHUNK_LIST, description = ATTRIBUTE_SENTDET_CHUNK_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_CHUNK_SPANS, description = "Holds the sentence chunks list found in the flow file content, " +
                                                                                             "as a JSON spans list."),
                   @WritesAttribute(attribute = ATTRIBUTE_SENTDET_SENTENCE_PROBABILITIES, description = "Holds the probability for each sentence prediction, " +
                                                                                                        "as a JSON double list.")})
@EqualsAndHashCode(callSuper = true)
public class DetectSentences extends AbstractNlpProcessor<SentenceModel> {

    static final String ATTRIBUTE_SENTDET_CHUNK_COUNT = "nlp.sentdet.chunk.count";

    static final String ATTRIBUTE_SENTDET_CHUNK_LIST = "nlp.sentdet.chunk.list";

    static final String ATTRIBUTE_SENTDET_CHUNK_LIST_DESCRIPTION = "Holds the sentence chunks list found in the flow file content.";

    static final String ATTRIBUTE_SENTDET_CHUNK_SPANS = "nlp.sentdet.chunk.spans";

    static final String ATTRIBUTE_SENTDET_SENTENCE_PROBABILITIES = "nlp.sentdet.sentence.probabilities";

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(Stream.of(PROPERTY_TRAINING_LANGUAGE),
                                                                                        super.getSupportedPropertyDescriptors().stream())
                                                                                .collect(toList());

    public DetectSentences() {super(SentenceModel.class);}

    @Override
    protected Map<String, String> executeModel(ProcessContext context, String content, Map<String, String> attributes, SentenceModel model) {
        Map<String, String> evaluation = new HashMap<>();
        SentenceDetectorME detector = new SentenceDetectorME(model);

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
    protected SentenceModel trainModel(ValidationContext validationContext,
                                       Collection<ValidationResult> results,
                                       TrainingParameters trainingParameters,
                                       Charset charset,
                                       InputStreamFactory inputStreamFactory) throws IOException {
        final String trainingLanguage = validationContext.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        SentenceDetectorFactory factory = new SentenceDetectorFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream)) {
            return SentenceDetectorME.train(trainingLanguage, sampleStream, factory, trainingParameters);
        }
    }
}
