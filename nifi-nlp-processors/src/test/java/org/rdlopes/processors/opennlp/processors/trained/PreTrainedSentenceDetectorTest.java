package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedSentenceDetectorTest extends PreTrainedProcessorTest<PreTrainedSentenceDetector> {

    public PreTrainedSentenceDetectorTest() {
        super(PreTrainedSentenceDetector.class);
    }

    @Test
    public void shouldDetectSentenceFromTimesheetQuestion() throws URISyntaxException {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-sent.bin").toString());
        testRunner.enqueue(SAMPLE_CONTENT);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(SENTENCE_DETECTOR_SENTENCES_LIST_KEY);
        List<String> sentenceList = get(SENTENCE_DETECTOR_SENTENCES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        flowFile.assertAttributeExists(SENTENCE_DETECTOR_SENTENCES_SPAN_KEY);
        List<Span> sentenceSpans = get(SENTENCE_DETECTOR_SENTENCES_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(sentenceList).containsExactly(SAMPLE_SENTENCES);
        assertThat(sentenceSpans).containsExactly(
                new Span(0, 180, null), new Span(181, 324, null), new Span(325, 419, null), new Span(420, 430, null),
                new Span(431, 454, null), new Span(455, 597, null));
    }

}
