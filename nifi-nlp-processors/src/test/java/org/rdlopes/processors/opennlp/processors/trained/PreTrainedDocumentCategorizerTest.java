package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedDocumentCategorizerTest extends PreTrainedProcessorTest<PreTrainedDocumentCategorizer> {

    public PreTrainedDocumentCategorizerTest() {
        super(PreTrainedDocumentCategorizer.class);
    }

    @Test
    public void shouldCategorizeTweets() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-doccat.bin").getFile());
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        SENTDET_CHUNK_LIST.updateAttributesWithJson(attributes, singletonList("Have a nice day!"));

        testRunner.enqueue("Have a nice day!", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);
        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeExists(DOCCAT_CATEGORY_LIST.key);
        flowFile.assertAttributeEquals(DOCCAT_CATEGORY_BEST.key, "1");
        flowFile.assertAttributeExists(DOCCAT_SCORE_MAP.key);
        List<String> categoryList = DOCCAT_CATEGORY_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        Map<Double, Set<String>> probabilities = DOCCAT_SCORE_MAP.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<Map<Double, Set<String>>>() {});
        assertThat(categoryList).containsExactlyInAnyOrder("1", "0");
        assertThat(probabilities).contains(
                entry(0.5123318385650224, singleton("1")),
                entry(0.4876681614349776, singleton("0")));
    }
}
