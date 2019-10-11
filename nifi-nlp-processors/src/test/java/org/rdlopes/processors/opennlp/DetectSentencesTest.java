package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.DetectSentences.*;

public class DetectSentencesTest extends AbstractNlpProcessorTest {
    public DetectSentencesTest() {
        super(DetectSentences.class, true);
    }

    @Test
    public void shouldDetectSentenceFromTimesheetQuestion() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-sent.bin").getFile());
        testRunner.enqueue("did I report my time correctly?");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_SENTDET_CHUNK_COUNT, "1");
        flowFile.assertAttributeExists(ATTRIBUTE_SENTDET_CHUNK_LIST);
        List<String> chunkList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_SENTDET_CHUNK_LIST),
                                                     new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_SENTDET_CHUNK_SPANS);
        List<Span> chunkSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_SENTDET_CHUNK_SPANS),
                                                    new TypeToken<List<Span>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_SENTDET_SENTENCE_PROBABILITIES);
        List<Double> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_SENTDET_SENTENCE_PROBABILITIES),
                                                         new TypeToken<List<Double>>() {}.getType());

        assertThat(chunkList).containsExactly("did I report my time correctly?");
        assertThat(chunkSpans).containsExactly(new Span(0, 31, null));
        assertThat(probabilities).containsExactly(0.9973151014913963);
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-sent.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }

}
