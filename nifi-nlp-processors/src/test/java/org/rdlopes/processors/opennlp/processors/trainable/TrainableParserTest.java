package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
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
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, new String[]{
                "She", "was", "just", "another", "freighter", "from", "the ", "States", "and", "she", "seemed", "as", "commonplace", "as", "her", "name", "."});
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZER_TOKENS_LIST_KEY, attributes.get(TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(PARSER_PARSES_LIST_KEY);
        List<String> parsesList = get(PARSER_PARSES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});

        assertThat(parsesList).containsOnly(
                "(TOP (S (NP (DT She)) (VP (VBD was) (S (NP (ADJP (RB just) (DT another)) (NN freighter) (PP (IN from) (NP (NP (DT the ) (NNP States)) " +
                "(CC and) (NP (NN she))))) (VP (VBD seemed) (ADVP (RB as)) (ADJP (JJ commonplace) (PP (IN as) (NP (JJ her) (NN name))))))) (. .)))");
    }
}
