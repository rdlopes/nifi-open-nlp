package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.ml.maxent.GISTrainer;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainablePOSTaggerTest extends TrainableProcessorTest<TrainablePOSTagger> {

    public TrainablePOSTaggerTest() {
        super(TrainablePOSTagger.class);
    }

    @Test
    public void shouldTagPOS() {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getClass().getResource("/training/en-tagpos.train").getFile());
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ALGORITHM.descriptor, GISTrainer.MAXENT_VALUE);
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(100));
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(5));
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, Arrays.asList("The", "driver", "got", "badly", "injured", "."));

        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));
        flowFile.assertAttributeExists(TAGPOS_TAG_LIST.key);
        List<String> tagsList = TAGPOS_TAG_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        assertThat(tagsList).containsExactly("DT", "NN", "VBD", "RB", "VBN", ".");
    }
}
