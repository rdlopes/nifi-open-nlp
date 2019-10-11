package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.*;
import static org.rdlopes.processors.opennlp.Chunk.*;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;

public class ChunkTest extends AbstractNlpProcessorTest {

    public ChunkTest() {
        super(Chunk.class, true);
    }

    @Test
    public void shouldChunkTimesheetQuestion() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-chunker.bin").getFile());
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TAGPOS_TAG_LIST, new Gson().toJson(Arrays.asList("VBD", "PRP", "VB", "PRP$", "NN", ".")));
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList("did", "I", "report", "my", "time", "correctly?")));
        testRunner.enqueue("did I report my time correctly?", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_CHUNK_COUNT, "6");
        flowFile.assertAttributeExists(ATTRIBUTE_CHUNK_LIST);
        List<String> chunkList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_CHUNK_LIST), new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_CHUNK_SPANS);
        List<Span> chunkSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_CHUNK_SPANS), new TypeToken<List<Span>>() {}.getType());
        assertThat(chunkList).contains("O", "B-NP", "B-VP", "B-NP", "I-NP", "O");
        assertThat(chunkSpans).contains(
                new Span(1, 2, "NP"),
                new Span(2, 3, "VP"),
                new Span(3, 5, "NP")
        );
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-chunker.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }

}
