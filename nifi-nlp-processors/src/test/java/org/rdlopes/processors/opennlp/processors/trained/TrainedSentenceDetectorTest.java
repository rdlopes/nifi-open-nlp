package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainedSentenceDetectorTest extends AbstractPreTrainedProcessorTest<TrainedSentenceDetector> {

    public TrainedSentenceDetectorTest() {
        super(TrainedSentenceDetector.class);
    }

    @Test
    public void shouldDetectSentenceFromTimesheetQuestion() {
        setModelFilePath("/models/en-sent.bin");
        testRunner.enqueue(SAMPLE_CONTENT_VINKEN);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(SENTDET_CHUNK_LIST.key);
        flowFile.assertAttributeExists(SENTDET_SPAN_LIST.key);
        flowFile.assertAttributeExists(SENTDET_PROBABILITIES.key);

        List<String> chunkList = SENTDET_CHUNK_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> chunkSpans = SENTDET_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});
        List<Double> probabilities = SENTDET_PROBABILITIES.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Double>>() {});

        assertThat(chunkList).containsExactly("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .",
                                              "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .",
                                              "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                                              "    a director of this British industrial conglomerate .");
        assertThat(chunkSpans).containsExactly(
                new Span(0, 87, null),
                new Span(88, 158, null),
                new Span(159, 308, null));
        assertThat(probabilities).containsExactly(
                0.9988144759309677,
                0.9806994380754595,
                0.9945708899749325);
    }

}
