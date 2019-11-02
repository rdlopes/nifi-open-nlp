package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.PARSER_PARSE_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPProperty.PARSER_PARSES_COUNT;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedParserTest extends PreTrainedProcessorTest<PreTrainedParser> {

    public PreTrainedParserTest() {
        super(PreTrainedParser.class);
    }

    @Test
    public void shouldParse() {
        setModelFilePath("/models/en-parser-chunking.bin");
        testRunner.setProperty(PARSER_PARSES_COUNT.descriptor, "3");
        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, new String[]{
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive", "director",
                "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch",
                "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former", "chairman", "of", "Consolidated",
                "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", "."});
        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));

        flowFile.assertAttributeExists(PARSER_PARSE_LIST.key);
        List<String> parsesList = PARSER_PARSE_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        assertThat(parsesList).containsExactly(
                "(TOP (S (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) (NN board)) (PP (IN as) (NP (DT a) " +
                "(JJ nonexecutive) (NN director) (NNP Nov) (NNP .) (CD 29) (. .) (NNP Mr) (. .) (NNP Vinken)))))) (VP (VBZ is) (NP (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Elsevier) (NNP N) " +
                "(NNP .) (NNP V) (NNP .)))) (, ,) (NP (DT the) (JJ Dutch) (NN publishing) (NN group))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (PP (S (UCP (ADJP (NP (CD 55) (NNS years)) " +
                "(JJ old)) (CC and) (S (NP (NP (JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) (VP (VBN named) (S (NP (NP " +
                "(DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate)))))))))))) (. .)))",
                "(TOP (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) (NN board)) (SBAR (IN as) (S (NP (DT a)" +
                " (JJ nonexecutive) (NN director) (NNP Nov) (NNP .) (CD 29) (. .) (NNP Mr) (. .) (NNP Vinken)) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NP (NNP Elsevier) (NNP N) (NNP .)" +
                " (NNP V) (NNP .)) (, ,) (NP (DT the) (JJ Dutch) (NN publishing) (NN group))))))))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (SBAR (S (UCP (ADJP (NP (CD 55) (NNS years)) " +
                "(JJ old)) (CC and) (S (NP (NP (JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) (VP (VBN named) (S (NP (NP " +
                "(DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate)))))))))))) (. .)))",
                "(TOP (S (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) (NN board)) (PP (IN as) (NP (DT a) " +
                "(JJ nonexecutive) (NN director) (NNP Nov) (NNP .) (CD 29) (. .) (NNP Mr) (. .) (NNP Vinken)))))) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NP (NNP Elsevier) (NNP N) " +
                "(NNP .) (NNP V) (NNP .)) (, ,) (NP (DT the) (JJ Dutch) (NN publishing) (NN group))))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (SBAR (S (UCP (ADJP (NP (CD 55) (NNS years)) " +
                "(JJ old)) (CC and) (S (NP (NP (JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) (VP (VBN named) (S (NP (NP " +
                "(DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate)))))))))))) (. .)))");
    }

}
