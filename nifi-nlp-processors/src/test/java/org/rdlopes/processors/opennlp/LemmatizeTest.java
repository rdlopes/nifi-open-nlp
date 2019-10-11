package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Sequence;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.Lemmatize.*;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;

public class LemmatizeTest extends AbstractNlpProcessorTest {
    public LemmatizeTest() {
        super(Lemmatize.class, true);
    }

    @Test
    public void shouldLemmatizeOpenNLPExample() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-lemmatizer.bin").getFile());
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TAGPOS_TAG_LIST,
                       new Gson().toJson(Arrays.asList("NNP", "NNP", ",", "CD", "NNS", "JJ", ",", "MD", "VB", "DT", "NN", "IN", "DT", "JJ", "NN", "NNP", ".", "CD", ".", "NNP", ".", "NNP", "VBZ",
                                                       "NN", "IN", "NNP", "NNP", ".", "NNP", ".", ",", "DT", "JJ", "NN", "NN", ".", "NNP", "NNP", ",", "CD", "NNS", "JJ", "CC", "JJ", "NN", "IN",
                                                       "NNP", "NNP", "NNP", "NNP", ",", "VBD", "VBN", "DT", "NN", "IN", "DT", "JJ", "JJ", "NN", ".")));
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST,
                       new Gson().toJson(Arrays.asList("Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive", "director",
                                                       "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch",
                                                       "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former", "chairman", "of", "Consolidated",
                                                       "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", ".")));

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_COUNT, "61");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_LIST);
        List<String> lemmaList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_LIST),
                                                     new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_COUNT, "1");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_LIST);
        List<List<String>> predictedList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_LIST),
                                                               new TypeToken<List<List<String>>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_COUNT, "61");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_LIST);
        List<String> sesPrediction = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_LIST),
                                                         new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_COUNT, "3");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_LIST);
        List<Sequence> topKLemmaClasses = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_LIST),
                                                              new TypeToken<List<Sequence>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_COUNT, "3");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_LIST);
        List<Sequence> topKSequences = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_LIST),
                                                           new TypeToken<List<Sequence>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_PROBABILITIES);
        List<Double> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_PROBABILITIES),
                                                         new TypeToken<List<Double>>() {}.getType());

        assertThat(lemmaList).containsExactly("pierre", "vinken", ",", "61", "year", "old", ",", "will", "join", "the", "board", "a", "a", "nonexecutive", "director", "nov", ".", "29", ".", "mr", ".",
                                              "vinken", "be", "chairman", "of", "elsevier", "n", ".", "v", ".", ",", "the", "dutch", "publishing", "group", ".", "rudolph", "agnew", ",", "55", "year",
                                              "old", "and", "former", "chairman", "of", "consolidated", "gold", "field", "plc", ",", "be", "name", "a", "director", "of", "this", "british",
                                              "industrial", "conglomerate", ".");
        assertThat(predictedList).containsExactly(
                Arrays.asList("pierre", "vinken", ",", "61", "year", "old", ",", "will", "join", "the", "board", "a", "a", "nonexecutive", "director", "nov", ".", "29", ".", "mr", ".",
                              "vinken", "be", "chairman", "of", "elsevier", "n", ".", "v", ".", ",", "the", "dutch", "publishing", "group", ".", "rudolph", "agnew", ",", "55", "year",
                              "old", "and", "former", "chairman", "of", "consolidated", "gold", "field", "plc", ",", "be", "name", "a", "director", "of", "this", "british",
                              "industrial", "conglomerate", "."));
        assertThat(sesPrediction).containsExactly("O", "O", "O", "O", "D0s", "O", "O", "O", "O", "O", "O", "D0s", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "R1ibR0se", "O", "O", "O", "O",
                                                  "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "D0s", "O", "O", "O", "O", "O", "O", "O", "D0s", "O", "O", "R2wbR1aeD0s", "D0d",
                                                  "O", "O", "O", "O", "O", "O", "O", "O");
        assertThat(topKLemmaClasses).hasSize(3);
        assertThat(topKSequences).hasSize(3);
        assertThat(probabilities).hasSize(61);
    }

    @Test
    public void shouldLemmatizeTimesheetQuestion() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-lemmatizer.bin").getFile());
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TAGPOS_TAG_LIST, new Gson().toJson(Arrays.asList("VBD", "PRP", "VB", "PRP$", "NN", ".")));
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList("did", "I", "report", "my", "time", "correctly?")));

        testRunner.enqueue("did I report my time correctly?", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_COUNT, "6");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_LIST);
        List<String> lemmaList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_LIST),
                                                     new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_COUNT, "1");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_LIST);
        List<List<String>> predictedList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_LIST),
                                                               new TypeToken<List<List<String>>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_COUNT, "6");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_LIST);
        List<String> sesPrediction = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_PREDICTED_SES_LIST),
                                                         new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_COUNT, "3");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_LIST);
        List<Sequence> topKLemmaClasses = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_LEMMA_LIST),
                                                              new TypeToken<List<Sequence>>() {}.getType());
        flowFile.assertAttributeEquals(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_COUNT, "3");
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_LIST);
        List<Sequence> topKSequences = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_TOPK_SEQUENCE_LIST),
                                                           new TypeToken<List<Sequence>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_LEMMATIZE_LEMMA_PROBABILITIES);
        List<Double> probabilities = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LEMMATIZE_LEMMA_PROBABILITIES),
                                                         new TypeToken<List<Double>>() {}.getType());

        assertThat(lemmaList).containsExactly("do", "i", "report", "my", "time", "correctly?");
        assertThat(predictedList).containsExactly(Arrays.asList("do", "i", "report", "my", "time", "correctly?"));
        assertThat(sesPrediction).containsExactly("R1ioD0d", "O", "O", "O", "O", "O");
        assertThat(topKLemmaClasses).hasSize(3);
        assertThat(topKSequences).hasSize(3);
        assertThat(probabilities).containsExactly(0.9989198125761588,
                                                  0.9871662910987283,
                                                  0.9946383424431352,
                                                  0.9987243590056064,
                                                  0.9950100533968365,
                                                  0.6742313709153928);
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-lemmatizer.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }
}
