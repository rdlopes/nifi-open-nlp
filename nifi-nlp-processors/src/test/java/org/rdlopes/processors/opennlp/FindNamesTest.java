package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.*;
import static org.rdlopes.processors.opennlp.FindNames.*;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;

public class FindNamesTest extends AbstractNlpProcessorTest {
    public FindNamesTest() {
        super(FindNames.class, true);
    }

    @Test
    public void shouldFindDatesFromSentence() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-ner-date.bin").getFile());
        testRunner.setProperty(PROPERTY_NAME_TYPE, "date");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive",
                "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".",
                ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
                "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this",
                "British", "industrial", "conglomerate", ".")));

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_LIST);
        List<String> nameList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_LIST),
                                                    new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_SPANS);
        List<Span> nameSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_SPANS),
                                                   new TypeToken<List<Span>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_PROBABILITIES);
        List<Double> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_PROBABILITIES),
                                                         new TypeToken<List<Double>>() {}.getType());

        assertThat(nameList).containsExactly("Nov . 29");
        assertThat(nameSpans).containsExactly(new Span(15, 18, "date"));
        assertThat(probabilities).hasSize(61);
    }

    @Test
    public void shouldFindLocationsFromSentence() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-ner-location.bin").getFile());
        testRunner.setProperty(PROPERTY_NAME_TYPE, "location");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive",
                "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".",
                ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
                "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this",
                "British", "industrial", "conglomerate", ".")));

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_LIST);
        List<String> nameList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_LIST),
                                                    new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_SPANS);
        List<Span> nameSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_SPANS),
                                                   new TypeToken<List<Span>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_PROBABILITIES);
        List<Double> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_PROBABILITIES),
                                                         new TypeToken<List<Double>>() {}.getType());

        assertThat(nameList).containsExactly("N . V .");
        assertThat(nameSpans).containsExactly(
                new Span(26, 30, "location"));
        assertThat(probabilities).hasSize(61);
    }

    @Test
    public void shouldFindOrganizationsPersonsFromSentence() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-ner-organization.bin").getFile());
        testRunner.setProperty(PROPERTY_NAME_TYPE, "organization");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive",
                "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".",
                ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
                "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this",
                "British", "industrial", "conglomerate", ".")));

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_LIST);
        List<String> nameList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_LIST),
                                                    new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_SPANS);
        List<Span> nameSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_SPANS),
                                                   new TypeToken<List<Span>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_PROBABILITIES);
        List<Double> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_PROBABILITIES),
                                                         new TypeToken<List<Double>>() {}.getType());

        assertThat(nameList).containsExactly("Consolidated Gold Fields PLC");
        assertThat(nameSpans).containsExactly(new Span(46, 50, "organization"));
        assertThat(probabilities).hasSize(61);
    }

    @Test
    public void shouldFindPersonsFromSentence() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-ner-person.bin").getFile());
        testRunner.setProperty(PROPERTY_NAME_TYPE, "person");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive",
                "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".",
                ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
                "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this",
                "British", "industrial", "conglomerate", ".")));

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_LIST);
        List<String> nameList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_LIST),
                                                    new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_NAME_SPANS);
        List<Span> nameSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_NAME_SPANS),
                                                   new TypeToken<List<Span>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_NAMEFIND_PROBABILITIES);
        List<Double> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_NAMEFIND_PROBABILITIES),
                                                         new TypeToken<List<Double>>() {}.getType());

        assertThat(nameList).containsExactly("Pierre Vinken", "Vinken", "Rudolph Agnew");
        assertThat(nameSpans).containsExactly(
                new Span(0, 2, "person"),
                new Span(21, 22, "person"),
                new Span(36, 38, "person"));
        assertThat(probabilities).hasSize(61);
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-ner-person.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }
}
