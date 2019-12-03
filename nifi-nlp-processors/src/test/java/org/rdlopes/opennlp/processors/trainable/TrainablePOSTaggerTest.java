package org.rdlopes.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.ml.maxent.GISTrainer;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.NLPAttribute;
import org.rdlopes.opennlp.common.NLPProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rdlopes.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainablePOSTaggerTest extends TrainableProcessorTest<TrainablePOSTagger> {

    public TrainablePOSTaggerTest() {
        super(TrainablePOSTagger.class, "/training/en-tagpos.train");
    }

    @Test
    public void shouldTagPOS() {
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_ALGORITHM.descriptor, GISTrainer.MAXENT_VALUE);
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(100));
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(5));
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, new String[]{"The", "driver", "got", "badly", "injured", "."});

        testRunner.enqueue("The driver got badly injured.", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes.get(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(NLPAttribute.POS_TAGGER_TAGS_LIST_KEY);
        List<String> tagsList = NLPAttribute.get(NLPAttribute.POS_TAGGER_TAGS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {
        });

        assertThat(tagsList).containsExactly("DT", "NN", "VBD", "RB", "VBN", ".");
    }
}
