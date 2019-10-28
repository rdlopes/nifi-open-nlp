package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainedPOSTaggerTest extends AbstractPreTrainedProcessorTest<TrainedPOSTagger> {

    public TrainedPOSTaggerTest() {
        super(TrainedPOSTagger.class);
    }

    @Test
    public void shouldTagPartOfSpeech() {
        setModelFilePath("/models/en-pos-maxent.bin");
        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, SAMPLE_TOKENS_VINKEN);
        testRunner.enqueue(SAMPLE_CONTENT_VINKEN, attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));
        flowFile.assertAttributeExists(TAGPOS_TAG_LIST.key);
        List<String> tagsList = TAGPOS_TAG_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        assertThat(tagsList).containsExactly(
                "NNP", "NNP", ",", "CD", "NNS", "JJ", ",", "MD", "VB", "DT", "NN", "IN", "DT", "JJ", "NN", "NNP", ".", "CD", ".", "NNP", ".", "NNP", "VBZ", "NN", "IN",
                "NNP", "NNP", ".", "NNP", ".", ",", "DT", "JJ", "NN", "NN", ".", "NNP", "NNP", ",", "CD", "NNS", "JJ", "CC", "JJ", "NN", "IN", "NNP", "NNP", "NNP", "NNP",
                ",", "VBD", "VBN", "DT", "NN", "IN", "DT", "JJ", "JJ", "NN", ".");
    }

}
