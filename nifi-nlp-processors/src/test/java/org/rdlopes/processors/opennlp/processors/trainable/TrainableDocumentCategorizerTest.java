package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static opennlp.tools.ml.naivebayes.NaiveBayesTrainer.NAIVE_BAYES_VALUE;
import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableDocumentCategorizerTest extends TrainableProcessorTest<TrainableDocumentCategorizer> {

    public TrainableDocumentCategorizerTest() {
        super(TrainableDocumentCategorizer.class);
    }

    @Test
    public void shouldCategorizeTweets() {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getClass().getResource("/training/en-doccat.train").getFile());
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ALGORITHM.descriptor, NAIVE_BAYES_VALUE);
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(0));
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        set(SENTENCE_DETECTOR_SENTENCES_LIST_KEY, attributes, singletonList("Have a nice day!"));

        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(SENTENCE_DETECTOR_SENTENCES_LIST_KEY, attributes.get(SENTENCE_DETECTOR_SENTENCES_LIST_KEY));
        flowFile.assertAttributeEquals(DOCUMENT_CATEGORIZER_CATEGORIES_BEST_KEY, "1");

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
