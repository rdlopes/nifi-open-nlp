package org.rdlopes.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rdlopes.opennlp.common.NLPAttribute.*;
import static org.rdlopes.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedPOSTaggerTest extends PreTrainedProcessorTest<PreTrainedPOSTagger> {

    public PreTrainedPOSTaggerTest() {
        super(PreTrainedPOSTagger.class);
    }

    @Test
    public void shouldTagPartOfSpeech() throws URISyntaxException {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-pos-maxent.bin").toString());
        Map<String, String> attributes = new HashMap<>();
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, SAMPLE_TOKENS_SIMPLE);

        testRunner.enqueue(SAMPLE_CONTENT, attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZER_TOKENS_LIST_KEY, attributes.get(TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(POS_TAGGER_TAGS_LIST_KEY);
        List<String> tagsList = get(POS_TAGGER_TAGS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});

        assertThat(tagsList).containsExactly(
                "NN", "VB", "NN", "IN", "DT", "NN", "MD", "VB", "VBN", "IN", "CD", ":", "CD", "VBP", ",", "CD", ":", "CD", "NNP", "CC", "CD", ":", "CD", "NNP", "NNP", "NNP", "NNP", ",", "CD", "NNS",
                "JJ", ",", "MD", "VB", "DT", "NN", "IN", "DT", "FW", ":", "NN", "NN", "NNP", ".", "CD", "NN", ".", "NNP", ".", "NNP", "VBZ", "NN", "IN", "NNP", "NNP", ".", "NNP", ".", ",", "DT", "JJ",
                "NN", "NN", "WDT", "VBZ", "CD", "NN", "IN", "VBN", "NNS", "IN", "DT", "NNP", "CC", "CD", "NN", "IN", "NNP", ".", "NNP", "NNP", ".", "NNP", ".", "RB", "VBZ", "CD", "NN", "IN", "DT",
                "JJ", "NN", "IN", "DT", "NN", ",", "NN", "JJR", "IN", "$", "CD", ".", "IN", "-LRB-", "LS", ".", "CD", ".", "CD", "NNS", ",", "CD", ".", "CD", "NNS", "-RRB-", ".", "NNP", "NNP", ",",
                "CD", "NNS", "JJ", "CC", "JJ", "NN", "IN", "NNP", "NNP", "NNP", "NNP", ",", "VBD", "VBN", "DT", "NN", "IN", "DT", "JJ", "JJ", "NN", ".");
    }

}
