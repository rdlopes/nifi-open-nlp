package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableChunkerTest extends AbstractTrainableProcessorTest<TrainableChunker> {

    public TrainableChunkerTest() {
        super(TrainableChunker.class);
    }

    @Test
    public void shouldChunk() {
        setTrainingFilePath("/training/en-chunker.train");
        Map<String, String> attributes = new HashMap<>();
        TAGPOS_TAG_LIST.updateAttributesWithJson(attributes, SAMPLE_TAGS_AGREEMENT);
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, SAMPLE_TOKENS_AGREEMENT);
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TAGPOS_TAG_LIST.key, attributes.get(TAGPOS_TAG_LIST.key));
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));
        flowFile.assertAttributeExists(CHUNK_CHUNK_LIST.key);
        flowFile.assertAttributeExists(CHUNK_SPAN_LIST.key);

        List<String> chunkList = CHUNK_CHUNK_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        assertThat(chunkList).containsExactly("B-NP", "B-VP", "B-NP", "I-NP", "B-VP", "B-SBAR",
                                              "B-NP", "B-VP", "I-VP", "B-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP",
                                              "I-NP", "O");

        List<Span> chunkSpans = CHUNK_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});
        assertThat(chunkSpans).containsExactly(
                new Span(0, 1, "NP"),
                new Span(1, 2, "VP"),
                new Span(2, 4, "NP"),
                new Span(4, 5, "VP"),
                new Span(5, 6, "SBAR"),
                new Span(6, 7, "NP"),
                new Span(7, 9, "VP"),
                new Span(9, 13, "NP"),
                new Span(13, 14, "PP"),
                new Span(14, 16, "NP"));
    }

}
