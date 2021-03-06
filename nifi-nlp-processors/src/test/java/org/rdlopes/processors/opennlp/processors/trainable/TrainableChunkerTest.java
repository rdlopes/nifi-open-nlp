package org.rdlopes.processors.opennlp.processors.trainable;

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
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINABLE_TRAINING_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableChunkerTest extends TrainableProcessorTest<TrainableChunker> {

    public TrainableChunkerTest() {
        super(TrainableChunker.class);
    }

    @Test
    public void shouldChunk() throws URISyntaxException {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath("/training/en-chunker.train").toString());
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        set(POS_TAGGER_TAGS_LIST_KEY, attributes, new String[]{
                "NNP", "VBD", "DT", "NN", "VBZ", "IN", "PRP", "TO", "VB", "CD", "JJ", "JJ", "NNS", "IN", "DT", "NNS", "."});
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, new String[]{
                "Rockwell", "said", "the", "agreement", "calls", "for", "it", "to", "supply", "200", "additional", "so-called", "shipsets", "for", "the", "planes", "."});

        testRunner.enqueue("", attributes);
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
                "B-NP", "B-VP", "B-NP", "I-NP", "B-VP", "B-SBAR", "B-NP", "B-VP", "I-VP", "B-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP", "I-NP", "O");
        assertThat(chunkSpans).containsExactly(
                new Span(0, 1, "NP"), new Span(1, 2, "VP"), new Span(2, 4, "NP"), new Span(4, 5, "VP"),
                new Span(5, 6, "SBAR"), new Span(6, 7, "NP"), new Span(7, 9, "VP"), new Span(9, 13, "NP"),
                new Span(13, 14, "PP"), new Span(14, 16, "NP"));
    }

}
