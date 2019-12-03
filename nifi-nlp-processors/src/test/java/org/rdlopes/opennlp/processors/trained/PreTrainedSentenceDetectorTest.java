package org.rdlopes.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.NLPAttribute;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PreTrainedSentenceDetectorTest extends PreTrainedProcessorTest<PreTrainedSentenceDetector> {

    public PreTrainedSentenceDetectorTest() {
        super(PreTrainedSentenceDetector.class, "/models/en-sent.bin");
    }

    @Test
    public void shouldDetectSentenceFromTimesheetQuestion() {
        testRunner.assertValid();

        testRunner.enqueue(SAMPLE_CONTENT);
        testRunner.run();
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(BaseProcessor.RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(NLPAttribute.SENTENCE_DETECTOR_SENTENCES_LIST_KEY);
        List<String> sentenceList = NLPAttribute.get(NLPAttribute.SENTENCE_DETECTOR_SENTENCES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {
        });
        flowFile.assertAttributeExists(NLPAttribute.SENTENCE_DETECTOR_SENTENCES_SPAN_KEY);
        List<Span> sentenceSpans = NLPAttribute.get(NLPAttribute.SENTENCE_DETECTOR_SENTENCES_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {
        });

        assertThat(sentenceList).containsExactly(SAMPLE_SENTENCES);
        assertThat(sentenceSpans).containsExactly(
                new Span(0, 180, null), new Span(181, 324, null), new Span(325, 419, null), new Span(420, 430, null),
                new Span(431, 454, null), new Span(455, 597, null));
    }

}
