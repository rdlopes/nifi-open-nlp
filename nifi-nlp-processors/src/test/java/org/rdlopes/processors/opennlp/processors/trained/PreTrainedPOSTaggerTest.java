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
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedPOSTaggerTest extends PreTrainedProcessorTest<PreTrainedPOSTagger> {

    public PreTrainedPOSTaggerTest() {
        super(PreTrainedPOSTagger.class);
    }

    @Test
    public void shouldTagPartOfSpeech() {
        setModelFilePath("/models/en-pos-maxent.bin");
        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, new String[]{
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive", "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman",
                "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former", "chairman", "of", "Consolidated",
                "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", "."});
        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
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
