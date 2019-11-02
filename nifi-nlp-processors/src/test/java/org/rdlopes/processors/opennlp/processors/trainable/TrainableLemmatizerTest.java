package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Sequence;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableLemmatizerTest extends TrainableProcessorTest<TrainableLemmatizer> {

    public TrainableLemmatizerTest() {
        super(TrainableLemmatizer.class);
    }

    @Test
    public void shouldLemmatizeOpenNLPExample() {
        setTrainingFilePath("/training/en-lemma.train");
        setTrainingParamCutoff(5);
        setTrainingParamIterations(100);
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        TAGPOS_TAG_LIST.updateAttributesWithJson(attributes, new String[]{
                "NNP", "VBD", "DT", "NN", "VBZ", "IN", "PRP", "TO", "VB", "CD", "JJ", "JJ", "NNS", "IN", "DT", "NNS", "."});
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, new String[]{
                "Rockwell", "said", "the", "agreement", "calls", "for", "it", "to", "supply", "200", "additional", "so-called", "shipsets", "for", "the", "planes", "."});
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TAGPOS_TAG_LIST.key, attributes.get(TAGPOS_TAG_LIST.key));
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));

        flowFile.assertAttributeExists(LEMMATIZE_LEMMA_LIST.key);
        flowFile.assertAttributeExists(LEMMATIZE_PREDICTED_LIST.key);
        flowFile.assertAttributeExists(LEMMATIZE_PREDICTED_SES_LIST.key);
        flowFile.assertAttributeExists(LEMMATIZE_PROBABILITIES.key);
        flowFile.assertAttributeExists(LEMMATIZE_TOPK_LIST.key);
        flowFile.assertAttributeExists(LEMMATIZE_TOPK_SEQUENCE_LIST.key);

        List<String> lemmaList = LEMMATIZE_LEMMA_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        String[][] predictedList = LEMMATIZE_PREDICTED_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<String[][]>() {});
        List<String> sesPrediction = LEMMATIZE_PREDICTED_SES_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Double> probabilities = LEMMATIZE_PROBABILITIES.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Double>>() {});
        List<Sequence> topKLemmaClasses = LEMMATIZE_TOPK_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Sequence>>() {});
        List<Sequence> topKSequences = LEMMATIZE_TOPK_SEQUENCE_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Sequence>>() {});

        assertThat(lemmaList).containsExactly(
                "rockwell", "say", "the", "agreement", "call", "for", "it", "to", "supply", "200", "additional", "so-called", "shipset", "for", "the", "plane", ".");
        assertThat(predictedList).containsExactly(
                new String[]{"rockwell", "say", "the", "agreement", "call", "for", "it", "to", "supply", "200", "additional", "so-called", "shipset", "for", "the", "plane", "."});
        assertThat(sesPrediction).containsExactly(
                "O", "R1iyD0d", "O", "O", "D0s", "O", "O", "O", "O", "O", "O", "O", "D0s", "O", "O", "D0s", "O");
        assertThat(topKLemmaClasses).isEmpty();
        assertThat(topKSequences).isEmpty();
        assertThat(probabilities).containsExactly(
                0.84636249564983, 0.9642884148585946, 0.9869961089047586, 0.9683765503961519, 0.6035075601072414, 0.9922389993033468, 0.9996382065737838, 0.99865955453056, 0.9824145909328867,
                0.9660370563256432, 0.9858427892073484, 0.9378488771456445, 0.9550815015935848, 0.9922389993033468, 0.9970268740855731, 0.8971222226474688, 0.9945185581140088);
    }
}
