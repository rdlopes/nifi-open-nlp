package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static opennlp.tools.ml.naivebayes.NaiveBayesTrainer.NAIVE_BAYES_VALUE;
import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.CategorizeDocument.*;

public class CategorizeDocumentTest extends AbstractNlpProcessorTest {

    public CategorizeDocumentTest() {
        super(CategorizeDocument.class, true);
    }

    @Test
    public void shouldAnalyzeSentimentFromTweetWithTrainingFile() {
        testRunner.setProperty(PROPERTY_TRAINING_CUTOFF, String.valueOf(0));
        testRunner.setProperty(PROPERTY_TRAINING_ALGORITHM, NAIVE_BAYES_VALUE);
        testRunner.setProperty(PROPERTY_TRAINING_FILE_PATH, getClass().getResource("/training/tweets.txt").getFile());
        testRunner.enqueue("Have a nice day!");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(ATTRIBUTE_DOCCAT_CATEGORY_LIST);
        List<String> categoryList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_DOCCAT_CATEGORY_LIST),
                                                        new TypeToken<List<String>>() {}.getType());

        flowFile.assertAttributeExists(ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP);
        Map<Double, Set<String>> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP),
                                                                     new TypeToken<Map<Double, Set<String>>>() {}.getType());

        flowFile.assertAttributeEquals(ATTRIBUTE_DOCCAT_CATEGORY_BEST, "1");
        flowFile.assertAttributeEquals(ATTRIBUTE_DOCCAT_CATEGORY_COUNT, "2");
        assertThat(categoryList).containsExactlyInAnyOrder("1", "0");
        assertThat(probabilities).hasSize(2);
    }

    @Test
    public void shouldCategorizeTimesheetQuestionFromTrainingFile() {
        testRunner.setProperty(PROPERTY_TRAINING_CUTOFF, String.valueOf(0));
        testRunner.setProperty(PROPERTY_TRAINING_ALGORITHM, NAIVE_BAYES_VALUE);
        testRunner.setProperty(PROPERTY_TRAINING_FILE_PATH, getClass().getResource("/training/doccat.train").getFile());
        testRunner.enqueue("did I report my time correctly?");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(ATTRIBUTE_DOCCAT_CATEGORY_LIST);
        List<String> categoryList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_DOCCAT_CATEGORY_LIST),
                                                        new TypeToken<List<String>>() {}.getType());

        flowFile.assertAttributeExists(ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP);
        Map<Double, Set<String>> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP),
                                                                     new TypeToken<Map<Double, Set<String>>>() {}.getType());

        flowFile.assertAttributeEquals(ATTRIBUTE_DOCCAT_CATEGORY_BEST, "timesheet");
        flowFile.assertAttributeEquals(ATTRIBUTE_DOCCAT_CATEGORY_COUNT, "2");
        assertThat(categoryList).contains("timesheet", "unmatched");
        assertThat(probabilities).hasSize(2);
    }

    @Test
    public void shouldCategorizeTimesheetQuestionWithNaiveBayesian() {
        testRunner.setProperty(PROPERTY_TRAINING_CUTOFF, String.valueOf(0));
        testRunner.setProperty(PROPERTY_TRAINING_ALGORITHM, NAIVE_BAYES_VALUE);
        testRunner.setProperty(PROPERTY_TRAINING_DATA,
                               "unmatched\tunmatched\n" +
                               "timesheet\tdid I report my time correctly?\n" +
                               "timesheet\tdid I report my tempo correctly?\n" +
                               "timesheet\tIs my time report ok?\n" +
                               "timesheet\tIs my time ok?\n" +
                               "timesheet\tIs my tempo ok?\n" +
                               "timesheet\tAm I good with my time report?\n" +
                               "timesheet\tAm I good with my timesheet?\n" +
                               "timesheet\tAm I good with my tempo?\n" +
                               "timesheet\tIs my timesheet correct?\n" +
                               "timesheet\tIs my time correct?\n" +
                               "timesheet\tIs my tempo correct?\n");
        testRunner.enqueue("Is my timesheet OK?");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(ATTRIBUTE_DOCCAT_CATEGORY_LIST);
        List<String> categoryList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_DOCCAT_CATEGORY_LIST),
                                                        new TypeToken<List<String>>() {}.getType());

        flowFile.assertAttributeExists(ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP);
        Map<Double, Set<String>> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP),
                                                                     new TypeToken<Map<Double, Set<String>>>() {}.getType());

        flowFile.assertAttributeEquals(ATTRIBUTE_DOCCAT_CATEGORY_BEST, "timesheet");
        flowFile.assertAttributeEquals(ATTRIBUTE_DOCCAT_CATEGORY_COUNT, "2");
        assertThat(categoryList).contains("timesheet", "unmatched");
        assertThat(probabilities).hasSize(2);
    }

    @Test
    public void shouldNotBeValidWithSingleOutcome() {
        testRunner.setProperty(PROPERTY_TRAINING_DATA,
                               "timesheet\tdid I report my time correctly?\n" +
                               "timesheet\tdid I report my tempo correctly?\n" +
                               "timesheet\tIs my time report ok?\n" +
                               "timesheet\tIs my time ok?\n" +
                               "timesheet\tIs my tempo ok?\n" +
                               "timesheet\tAm I good with my time report?\n" +
                               "timesheet\tAm I good with my timesheet?\n" +
                               "timesheet\tAm I good with my tempo?\n" +
                               "timesheet\tIs my timesheet correct?\n" +
                               "timesheet\tIs my timesheet correct?\n" +
                               "timesheet\tIs my timesheet correct?\n");
        testRunner.assertNotValid();
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_TRAINING_CUTOFF, String.valueOf(0));
        testRunner.setProperty(PROPERTY_TRAINING_ALGORITHM, NAIVE_BAYES_VALUE);
        testRunner.setProperty(PROPERTY_TRAINING_DATA,
                               "unmatched\tunmatched\n" +
                               "timesheet\tdid I report my time correctly?\n" +
                               "timesheet\tdid I report my tempo correctly?\n" +
                               "timesheet\tIs my time report ok?\n" +
                               "timesheet\tIs my time ok?\n" +
                               "timesheet\tIs my tempo ok?\n" +
                               "timesheet\tAm I good with my time report?\n" +
                               "timesheet\tAm I good with my timesheet?\n" +
                               "timesheet\tAm I good with my tempo?\n" +
                               "timesheet\tIs my timesheet correct?\n" +
                               "timesheet\tIs my time correct?\n" +
                               "timesheet\tIs my tempo correct?\n");
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }

}
