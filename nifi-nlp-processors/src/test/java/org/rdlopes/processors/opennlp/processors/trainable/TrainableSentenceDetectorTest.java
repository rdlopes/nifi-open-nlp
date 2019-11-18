package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableSentenceDetectorTest extends TrainableProcessorTest<TrainableSentenceDetector> {

    public TrainableSentenceDetectorTest() {
        super(TrainableSentenceDetector.class);
    }

    @Test
    public void shouldDetectSentenceFromTimesheetQuestion() throws URISyntaxException {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath("/training/en-sentdet.train").toString());
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(100));
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(0));
        testRunner.assertValid();

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(SENTENCE_DETECTOR_SENTENCES_LIST_KEY);
        List<String> sentenceList = get(SENTENCE_DETECTOR_SENTENCES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        flowFile.assertAttributeExists(SENTENCE_DETECTOR_SENTENCES_SPAN_KEY);
        List<Span> sentenceSpans = get(SENTENCE_DETECTOR_SENTENCES_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(sentenceList).containsExactly(
                "Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov.",
                "29 .",
                "Mr.",
                "Vinken is chairman of Elsevier N.V.",
                ", the Dutch publishing group .",
                "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                "    a director of this British industrial conglomerate .");
        assertThat(sentenceSpans).containsExactly(
                new Span(0, 82, null), new Span(83, 87, null), new Span(88, 91, null),
                new Span(92, 127, null), new Span(128, 158, null), new Span(159, 308, null));
    }

}
