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

public class TrainableParserTest extends TrainableProcessorTest<TrainableParser> {

    public TrainableParserTest() {
        super(TrainableParser.class, "/training/en-parser.train");
    }

    @Test
    public void shouldParse() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.PARSER_HEAD_RULES_FILE_PATH.descriptor, getFilePath("/training/en_head_rules").toString());
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, new String[]{
                "She", "was", "just", "another", "freighter", "from", "the ", "States", "and", "she", "seemed", "as", "commonplace", "as", "her", "name", "."});
        testRunner.enqueue("", attributes);
        testRunner.run();
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(BaseProcessor.RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes.get(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(NLPAttribute.PARSER_PARSES_LIST_KEY);
        List<String> parsesList = NLPAttribute.get(NLPAttribute.PARSER_PARSES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {
        });

        assertThat(parsesList).containsOnly(
                "(TOP (S (NP (DT She)) (VP (VBD was) (S (NP (ADJP (RB just) (DT another)) (NN freighter) (PP (IN from) (NP (NP (DT the ) (NNP States)) " +
                        "(CC and) (NP (NN she))))) (VP (VBD seemed) (ADVP (RB as)) (ADJP (JJ commonplace) (PP (IN as) (NP (JJ her) (NN name))))))) (. .)))");
    }
}
