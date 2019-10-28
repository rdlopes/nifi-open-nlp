package org.rdlopes.processors.opennlp.processors.trained;

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

public class TrainedChunkerTest extends AbstractPreTrainedProcessorTest<TrainedChunker> {
    public TrainedChunkerTest() {
        super(TrainedChunker.class);
    }

    @Test
    public void shouldChunk() {
        setModelFilePath("/models/en-chunker.bin");
        Map<String, String> attributes = new HashMap<>();
        TAGPOS_TAG_LIST.updateAttributesWithJson(attributes, SAMPLE_TAGS_VINKEN);
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, SAMPLE_TOKENS_VINKEN);
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
        assertThat(chunkList).containsExactly("B-NP", "I-NP", "O", "B-NP", "I-NP", "B-ADJP", "O", "B-VP", "I-VP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "B-NP", "O", "O", "B-NP", "I-NP", "O",
                                              "B-NP", "B-VP", "B-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "O", "O", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-NP", "I-NP", "O", "B-NP", "I-NP",
                                              "B-ADJP", "O", "B-ADVP", "B-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-VP", "I-VP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP",
                                              "O");

        List<Span> chunkSpans = CHUNK_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});
        assertThat(chunkSpans).containsExactly(
                new Span(0, 2, "NP"), new Span(3, 5, "NP"), new Span(5, 6, "ADJP"), new Span(7, 9, "VP"), new Span(9, 11, "NP"),
                new Span(11, 12, "PP"), new Span(12, 15, "NP"), new Span(15, 16, "NP"), new Span(18, 20, "NP"), new Span(21, 22, "NP"),
                new Span(22, 23, "VP"), new Span(23, 24, "NP"), new Span(24, 25, "PP"), new Span(25, 29, "NP"), new Span(31, 35, "NP"),
                new Span(36, 38, "NP"), new Span(39, 41, "NP"), new Span(41, 42, "ADJP"), new Span(43, 44, "ADVP"), new Span(44, 45, "NP"),
                new Span(45, 46, "PP"), new Span(46, 50, "NP"), new Span(51, 53, "VP"), new Span(53, 55, "NP"), new Span(55, 56, "PP"),
                new Span(56, 60, "NP"));
    }

}
