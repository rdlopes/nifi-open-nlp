package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedChunkerTest extends PreTrainedProcessorTest<PreTrainedChunker> {
    public PreTrainedChunkerTest() {
        super(PreTrainedChunker.class);
    }

    @Test
    public void shouldChunk() throws URISyntaxException {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-chunker.bin").toString());
        Map<String, String> attributes = new HashMap<>();
        set(POS_TAGGER_TAGS_LIST_KEY, attributes, SAMPLE_TAGS_SIMPLE);
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, SAMPLE_TOKENS_SIMPLE);

        testRunner.enqueue(SAMPLE_CONTENT, attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(POS_TAGGER_TAGS_LIST_KEY, attributes.get(POS_TAGGER_TAGS_LIST_KEY));
        flowFile.assertAttributeEquals(TOKENIZER_TOKENS_LIST_KEY, attributes.get(TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(CHUNKER_CHUNKS_LIST_KEY);
        List<String> chunkList = get(CHUNKER_CHUNKS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        flowFile.assertAttributeExists(CHUNKER_CHUNKS_SPAN_KEY);
        List<Span> chunkSpans = get(CHUNKER_CHUNKS_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(chunkList).containsExactly(
                "B-NP", "B-VP", "B-NP", "B-PP", "B-NP", "I-NP", "B-VP", "I-VP", "I-VP", "B-PP", "B-NP", "O", "B-NP", "I-NP", "O", "B-NP", "O", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-NP", "I-NP",
                "I-NP", "I-NP", "I-NP", "O", "B-NP", "I-NP", "B-ADJP", "O", "B-VP", "I-VP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "O", "B-NP", "I-NP", "B-NP", "O", "B-NP", "I-NP", "O", "O", "O",
                "B-NP", "B-VP", "B-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "O", "O", "B-NP", "I-NP", "I-NP", "I-NP", "B-NP", "B-VP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "B-PP", "B-NP",
                "I-NP", "O", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "O", "B-VP", "I-VP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP",
                "O", "B-VP", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-ADVP", "B-NP", "I-NP", "B-NP", "I-NP", "O", "B-NP", "I-NP", "O", "B-NP", "O", "B-NP", "I-NP", "O", "B-NP", "I-NP", "I-NP", "O",
                "B-NP", "I-NP", "B-ADJP", "O", "B-ADVP", "B-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-VP", "I-VP", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "I-NP", "O");

        assertThat(chunkSpans).containsExactly(
                new Span(0, 1, "NP"), new Span(1, 2, "VP"), new Span(2, 3, "NP"), new Span(3, 4, "PP"), new Span(4, 6, "NP"),
                new Span(6, 9, "VP"), new Span(9, 10, "PP"), new Span(10, 11, "NP"), new Span(12, 14, "NP"), new Span(15, 16, "NP"),
                new Span(17, 21, "NP"), new Span(22, 27, "NP"), new Span(28, 30, "NP"), new Span(30, 31, "ADJP"), new Span(32, 34, "VP"),
                new Span(34, 36, "NP"), new Span(36, 37, "PP"), new Span(37, 39, "NP"), new Span(40, 42, "NP"), new Span(42, 43, "NP"),
                new Span(44, 46, "NP"), new Span(49, 50, "NP"), new Span(50, 51, "VP"), new Span(51, 52, "NP"), new Span(52, 53, "PP"),
                new Span(53, 57, "NP"), new Span(59, 63, "NP"), new Span(63, 64, "NP"), new Span(64, 65, "VP"), new Span(65, 67, "NP"),
                new Span(67, 68, "PP"), new Span(68, 70, "NP"), new Span(70, 71, "PP"), new Span(71, 73, "NP"), new Span(74, 76, "NP"),
                new Span(76, 77, "PP"), new Span(77, 83, "NP"), new Span(84, 86, "VP"), new Span(86, 88, "NP"), new Span(88, 89, "PP"),
                new Span(89, 92, "NP"), new Span(92, 93, "PP"), new Span(93, 95, "NP"), new Span(96, 97, "VP"), new Span(97, 101, "NP"),
                new Span(102, 103, "ADVP"), new Span(103, 105, "NP"), new Span(105, 107, "NP"), new Span(108, 110, "NP"),
                new Span(111, 112, "NP"), new Span(113, 115, "NP"), new Span(116, 119, "NP"), new Span(120, 122, "NP"),
                new Span(122, 123, "ADJP"), new Span(124, 125, "ADVP"), new Span(125, 126, "NP"), new Span(126, 127, "PP"),
                new Span(127, 131, "NP"), new Span(132, 134, "VP"), new Span(134, 136, "NP"), new Span(136, 137, "PP"),
                new Span(137, 141, "NP")
        );
    }


}
