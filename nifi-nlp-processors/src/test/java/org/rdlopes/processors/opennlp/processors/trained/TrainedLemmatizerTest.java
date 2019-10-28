package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Sequence;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainedLemmatizerTest extends AbstractPreTrainedProcessorTest<TrainedLemmatizer> {
    public TrainedLemmatizerTest() {
        super(TrainedLemmatizer.class);
    }

    @Test
    public void shouldLemmatizeOpenNLPExample() {
        setModelFilePath("/models/en-lemmatizer.bin");
        Map<String, String> attributes = new HashMap<>();
        TAGPOS_TAG_LIST.updateAttributesWithJson(attributes, SAMPLE_TAGS_VINKEN);
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, SAMPLE_TOKENS_VINKEN);
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

        assertThat(lemmaList).containsExactly("pierre", "vinken", ",", "61", "year", "old", ",", "will", "join", "the", "board", "a", "a", "nonexecutive", "director", "nov", ".", "29", ".", "mr", ".",
                                              "vinken", "be", "chairman", "of", "elsevier", "n", ".", "v", ".", ",", "the", "dutch", "publishing", "group", ".", "rudolph", "agnew", ",", "55", "year",
                                              "old", "and", "former", "chairman", "of", "consolidated", "gold", "field", "plc", ",", "be", "name", "a", "director", "of", "this", "british",
                                              "industrial", "conglomerate", ".");
        assertThat(predictedList).containsExactly(new String[]{"pierre", "vinken", ",", "61", "year", "old", ",", "will", "join", "the", "board", "a", "a", "nonexecutive", "director", "nov", ".",
                                                               "29", ".", "mr", ".", "vinken", "be", "chairman", "of", "elsevier", "n", ".", "v", ".", ",", "the", "dutch", "publishing", "group", ".",
                                                               "rudolph", "agnew", ",", "55", "year", "old", "and", "former", "chairman", "of", "consolidated", "gold", "field", "plc", ",", "be",
                                                               "name", "a", "director", "of", "this", "british", "industrial", "conglomerate", "."});
        assertThat(sesPrediction).containsExactly("O", "O", "O", "O", "D0s", "O", "O", "O", "O", "O", "O", "D0s", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "R1ibR0se", "O", "O", "O",
                                                  "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "D0s", "O", "O", "O", "O", "O", "O", "O", "D0s", "O", "O", "R2wbR1aeD0s",
                                                  "D0d", "O", "O", "O", "O", "O", "O", "O", "O");
        assertThat(topKLemmaClasses).isEmpty();
        assertThat(topKSequences).isEmpty();
        assertThat(probabilities).containsExactly(0.8300777693926765, 0.9569824661553638, 0.3608378957255508, 0.7542626480266423, 0.995265076493792, 0.8994162870303829, 0.3608378957255508,
                                                  0.9936747741091305, 0.9963730665961043, 0.9963036611181606, 0.9983744469637763, 0.6795208673211424, 0.9950136956685746, 0.9979770925052145,
                                                  0.9965667908076049, 0.908727951713627, 0.3608378957255508, 0.7542626480266423, 0.3608378957255508, 0.6806141478482268, 0.3608378957255508,
                                                  0.9569824661553638, 0.9591679007338904, 0.9783871433563601, 0.99794660486631, 0.8868128683222061, 0.6806141478482268, 0.3608378957255508,
                                                  0.6806141478482268, 0.3608378957255508, 0.3608378957255508, 0.9963036611181606, 0.9960531327118173, 0.9884017556457902, 0.9988576100468689,
                                                  0.3608378957255508, 0.940144371373095, 0.8715521581058876, 0.3608378957255508, 0.7542626480266423, 0.995265076493792, 0.8994162870303829,
                                                  0.9976063893160992, 0.9954314579890313, 0.9962772441966633, 0.99794660486631, 0.7882118969854588, 0.9183729910012821, 0.428571817971671,
                                                  0.7387294307106608, 0.3608378957255508, 0.9616924483977343, 0.9309394165011591, 0.9789018058799092, 0.9965667908076049, 0.99794660486631,
                                                  0.974836534716637, 0.9990691381493793, 0.9930567135187909, 0.9973919769430756, 0.3608378957255508);
    }

}
