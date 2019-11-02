package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableTokenNameFinderTest extends TrainableProcessorTest<TrainableTokenNameFinder> {

    public TrainableTokenNameFinderTest() {
        super(TrainableTokenNameFinder.class);
    }

    private void assertProcessorCanFind(String nameType,
                                        Consumer<List<String>> nameListAssertion,
                                        Consumer<List<Span>> spanListAssertion,
                                        Consumer<List<Double>> probabilitiesAssertion) {
        testRunner.setProperty(NAMEFIND_NAME_TYPE.descriptor, nameType);
        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, new String[]{
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive", "director",
                "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch",
                "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former", "chairman", "of", "Consolidated",
                "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", "."});
        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TAGPOS_TAG_LIST.key, attributes.get(TAGPOS_TAG_LIST.key));
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));

        flowFile.assertAttributeExists(NAMEFIND_NAME_LIST.key);
        flowFile.assertAttributeExists(NAMEFIND_SPAN_LIST.key);
        flowFile.assertAttributeExists(NAMEFIND_PROBABILITIES.key);

        List<String> nameList = NAMEFIND_NAME_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> nameSpans = NAMEFIND_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});
        List<Double> probabilities = NAMEFIND_PROBABILITIES.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Double>>() {});

        nameListAssertion.accept(nameList);
        spanListAssertion.accept(nameSpans);
        probabilitiesAssertion.accept(probabilities);
    }

    @Test
    public void shouldFindDates() {
        setTrainingFilePath("/training/en-namefind.train");
        setTrainingParamCutoff(1);
        setTrainingParamIterations(70);

        testRunner.setProperty(NAMEFIND_NAME_TYPE.descriptor, "default");

        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, Arrays.asList(
                "Hi", "Mike", ",", "it's", "Stefanie", "Schmidt", "."));
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TAGPOS_TAG_LIST.key, attributes.get(TAGPOS_TAG_LIST.key));
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));

        flowFile.assertAttributeExists(NAMEFIND_NAME_LIST.key);
        flowFile.assertAttributeExists(NAMEFIND_SPAN_LIST.key);
        flowFile.assertAttributeExists(NAMEFIND_PROBABILITIES.key);

        List<String> nameList = NAMEFIND_NAME_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> nameSpans = NAMEFIND_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});
        List<Double> probabilities = NAMEFIND_PROBABILITIES.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Double>>() {});
        assertThat(nameList).containsExactly(
                "Mike", "Stefanie Schmidt");
        assertThat(nameSpans).containsExactly(
                new Span(1, 2, "default"),
                new Span(4, 6, "default"));
        assertThat(probabilities).containsExactly(
                0.9357052060330299, 0.9629181679586647, 0.987971659893104, 0.9949441347953653,
                0.9623310796558195, 0.9616806156295147, 0.9875749552188686
        );
    }
}
