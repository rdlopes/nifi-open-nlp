package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.PARSER_PARSE_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPProperty.PARSER_HEAD_RULES_FILE_PATH;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINABLE_TRAINING_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableParserTest extends TrainableProcessorTest<TrainableParser> {

    public TrainableParserTest() {
        super(TrainableParser.class);
    }

    @Test
    public void shouldParse() {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getClass().getResource("/training/en-parser.train").getFile());
        testRunner.setProperty(PARSER_HEAD_RULES_FILE_PATH.descriptor, getClass().getResource("/training/en_head_rules").getFile());
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, Arrays.asList(
                "She", "was", "just", "another", "freighter", "from", "the ",
                "States", "and", "she", "seemed", "as", "commonplace", "as", "her", "name", "."));
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));

        flowFile.assertAttributeExists(PARSER_PARSE_LIST.key);
        List<String> parsesList = PARSER_PARSE_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        assertThat(parsesList).containsOnly(
                "(TOP (S (NP (DT She)) (VP (VBD was) (S (NP (ADJP (RB just) (DT another)) (NN freighter) (PP (IN from) (NP (NP (DT the ) (NNP States)) " +
                "(CC and) (NP (NN she))))) (VP (VBD seemed) (ADVP (RB as)) (ADJP (JJ commonplace) (PP (IN as) (NP (JJ her) (NN name))))))) (. .)))");
    }
}
