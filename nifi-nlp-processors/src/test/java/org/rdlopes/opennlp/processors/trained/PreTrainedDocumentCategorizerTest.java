package org.rdlopes.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.rdlopes.opennlp.common.NLPAttribute.*;
import static org.rdlopes.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedDocumentCategorizerTest extends PreTrainedProcessorTest<PreTrainedDocumentCategorizer> {

    public PreTrainedDocumentCategorizerTest() {
        super(PreTrainedDocumentCategorizer.class);
    }

    @Test
    public void shouldCategorizeTweets() throws URISyntaxException {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-doccat.bin").toString());
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        set(SENTENCE_DETECTOR_SENTENCES_LIST_KEY, attributes, singletonList("Have a nice day!"));

        testRunner.enqueue("Have a nice day!", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);
        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(DOCUMENT_CATEGORIZER_CATEGORIES_LIST_KEY);
        List<String> categoryList = get(DOCUMENT_CATEGORIZER_CATEGORIES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        flowFile.assertAttributeExists(DOCUMENT_CATEGORIZER_SCORE_MAP_KEY);
        Map<Double, Set<String>> scoreMap = get(DOCUMENT_CATEGORIZER_SCORE_MAP_KEY, flowFile.getAttributes(), new TypeToken<Map<Double, Set<String>>>() {});

        assertThat(categoryList).containsExactlyInAnyOrder("1", "0");
        assertThat(scoreMap).contains(
                entry(0.5123318385650224, singleton("1")),
                entry(0.4876681614349776, singleton("0")));
    }
}
