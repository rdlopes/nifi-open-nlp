package org.rdlopes.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.NLPAttribute;
import org.rdlopes.opennlp.common.NLPProperty;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainableLemmatizerTest extends TrainableProcessorTest<TrainableLemmatizer> {

    public TrainableLemmatizerTest() {
        super(TrainableLemmatizer.class);
    }

    @Test
    public void shouldLemmatizeOpenNLPExample() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath("/training/en-lemma.train").toString());
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(5));
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(100));
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        NLPAttribute.set(NLPAttribute.POS_TAGGER_TAGS_LIST_KEY, attributes, new String[]{
                "NNP", "VBD", "DT", "NN", "VBZ", "IN", "PRP", "TO", "VB", "CD", "JJ", "JJ", "NNS", "IN", "DT", "NNS", "."});
        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, new String[]{
                "Rockwell", "said", "the", "agreement", "calls", "for", "it", "to", "supply", "200", "additional", "so-called", "shipsets", "for", "the", "planes", "."});
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(BaseProcessor.RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(NLPAttribute.POS_TAGGER_TAGS_LIST_KEY, attributes.get(NLPAttribute.POS_TAGGER_TAGS_LIST_KEY));
        flowFile.assertAttributeEquals(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes.get(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(NLPAttribute.LEMMATIZER_LEMMAS_LIST_KEY);
        List<String> lemmaList = NLPAttribute.get(NLPAttribute.LEMMATIZER_LEMMAS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {
        });

        assertThat(lemmaList).containsExactly(
                "rockwell", "say", "the", "agreement", "call", "for", "it", "to", "supply", "200", "additional", "so-called", "shipset", "for", "the", "plane", ".");
    }
}
