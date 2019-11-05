package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableLemmatizerTest extends TrainableProcessorTest<TrainableLemmatizer> {

    public TrainableLemmatizerTest() {
        super(TrainableLemmatizer.class);
    }

    @Test
    public void shouldLemmatizeOpenNLPExample() {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getClass().getResource("/training/en-lemma.train").getFile());
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(5));
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(100));
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        set(POS_TAGGER_TAGS_LIST_KEY, attributes, new String[]{
                "NNP", "VBD", "DT", "NN", "VBZ", "IN", "PRP", "TO", "VB", "CD", "JJ", "JJ", "NNS", "IN", "DT", "NNS", "."});
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, new String[]{
                "Rockwell", "said", "the", "agreement", "calls", "for", "it", "to", "supply", "200", "additional", "so-called", "shipsets", "for", "the", "planes", "."});
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(POS_TAGGER_TAGS_LIST_KEY, attributes.get(POS_TAGGER_TAGS_LIST_KEY));
        flowFile.assertAttributeEquals(TOKENIZER_TOKENS_LIST_KEY, attributes.get(TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(LEMMATIZER_LEMMAS_LIST_KEY);
        List<String> lemmaList = get(LEMMATIZER_LEMMAS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});

        assertThat(lemmaList).containsExactly(
                "rockwell", "say", "the", "agreement", "call", "for", "it", "to", "supply", "200", "additional", "so-called", "shipset", "for", "the", "plane", ".");
    }
}
