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
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableDocumentCategorizerTest extends AbstractTrainableProcessorTest<TrainableDocumentCategorizer> {

    public TrainableDocumentCategorizerTest() {
        super(TrainableDocumentCategorizer.class);
    }

    @Test
    public void shouldCategorizeTweets() {
        setTrainingFilePath("/training/en-doccat.train");
        setTrainingParamAlgorithm(NAIVE_BAYES_VALUE);
        setTrainingParamCutoff(0);
        Map<String, String> attributes = new HashMap<>();
        SENTDET_CHUNK_LIST.updateAttributesWithJson(attributes, singletonList("Have a nice day!"));
        testRunner.enqueue("", attributes);
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
