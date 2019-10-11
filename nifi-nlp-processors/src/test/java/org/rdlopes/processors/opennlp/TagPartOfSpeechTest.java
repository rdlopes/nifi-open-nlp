package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.*;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_COUNT;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;

public class TagPartOfSpeechTest extends AbstractNlpProcessorTest {

    public TagPartOfSpeechTest() {
        super(TagPartOfSpeech.class, true);
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-pos-maxent.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }

    @Test
    public void shouldTagTimesheetQuestion() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-pos-maxent.bin").getFile());
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList("did", "I", "report", "my", "time", "correctly", "?")));
        testRunner.enqueue("did I report my time correctly?", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_TAGPOS_TAG_COUNT, String.valueOf(7));
        flowFile.assertAttributeExists(ATTRIBUTE_TAGPOS_TAG_LIST);
        List<String> tagsList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TAGPOS_TAG_LIST), new TypeToken<List<String>>() {}.getType());
        assertThat(tagsList).containsExactly("VBD", "PRP", "VB", "PRP$", "NN", "RB", ".");
    }

}
