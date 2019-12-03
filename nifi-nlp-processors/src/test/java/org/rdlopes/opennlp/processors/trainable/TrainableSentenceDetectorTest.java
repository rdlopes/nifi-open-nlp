package org.rdlopes.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.NLPAttribute;
import org.rdlopes.opennlp.common.NLPProperty;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainableSentenceDetectorTest extends TrainableProcessorTest<TrainableSentenceDetector> {

    public TrainableSentenceDetectorTest() {
        super(TrainableSentenceDetector.class);
    }

    @Test
    public void shouldDetectSentenceFromTimesheetQuestion() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath("/training/en-sentdet.train").toString());
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(100));
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(0));
        testRunner.assertValid();

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                "    a director of this British industrial conglomerate .");
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
