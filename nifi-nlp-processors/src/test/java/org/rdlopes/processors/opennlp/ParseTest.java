package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.*;
import static org.rdlopes.processors.opennlp.Parse.*;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;

public class ParseTest extends AbstractNlpProcessorTest {
    public ParseTest() {
        super(Parse.class, true);
    }

    @Test
    public void shouldParseOpenNLPExampleWithSingleParse() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-parser-chunking.bin").getFile());
        testRunner.setProperty(PROPERTY_NUM_PARSES, String.valueOf(3));
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive",
                "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".",
                ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
                "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this",
                "British", "industrial", "conglomerate", ".")));

        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_PARSER_PARSE_COUNT, String.valueOf(3));
        flowFile.assertAttributeExists(ATTRIBUTE_PARSER_PARSE_LIST);
        List<String> parsesList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_PARSER_PARSE_LIST), new TypeToken<List<String>>() {}.getType());

        assertThat(parsesList).containsExactly("(TOP (S (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) " +
                                               "(NN board)) (PP (IN as) (NP (DT a) (JJ nonexecutive) (NN director) (NNP Nov) (NNP .) (CD 29) (. .) (NNP Mr) (. .) (NNP Vinken)))))) (VP (VBZ is) " +
                                               "(NP (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Elsevier) (NNP N) (NNP .) (NNP V) (NNP .)))) (, ,) (NP (DT the) (JJ Dutch) (NN publishing) " +
                                               "(NN group))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (PP (S (UCP (ADJP (NP (CD 55) (NNS years)) (JJ old)) (CC and) (S (NP (NP (JJ former) " +
                                               "(NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) (VP (VBN named) (S (NP (NP (DT a) " +
                                               "(NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate)))))))))))) (. .)))",
                                               "(TOP (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) " +
                                               "(NN board)) (SBAR (IN as) (S (NP (DT a) (JJ nonexecutive) (NN director) (NNP Nov) (NNP .) (CD 29) (. .) (NNP Mr) (. .) (NNP Vinken)) (VP (VBZ is) " +
                                               "(NP (NP (NN chairman)) (PP (IN of) (NP (NP (NNP Elsevier) (NNP N) (NNP .) (NNP V) (NNP .)) (, ,) (NP (DT the) (JJ Dutch) (NN publishing) " +
                                               "(NN group))))))))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (SBAR (S (UCP (ADJP (NP (CD 55) (NNS years)) (JJ old)) (CC and) (S (NP (NP " +
                                               "(JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) (VP (VBN named) (S (NP " +
                                               "(NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate)))))))))))) (. .)))",
                                               "(TOP (S (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) " +
                                               "(NN board)) (PP (IN as) (NP (DT a) (JJ nonexecutive) (NN director) (NNP Nov) (NNP .) (CD 29) (. .) (NNP Mr) (. .) (NNP Vinken)))))) (VP (VBZ is) " +
                                               "(NP (NP (NN chairman)) (PP (IN of) (NP (NP (NNP Elsevier) (NNP N) (NNP .) (NNP V) (NNP .)) (, ,) (NP (DT the) (JJ Dutch) (NN publishing) " +
                                               "(NN group))))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (SBAR (S (UCP (ADJP (NP (CD 55) (NNS years)) (JJ old)) (CC and) (S" +
                                               " (NP (NP (JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) (VP (VBN named) " +
                                               "(S (NP (NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate)))))))))))) (. .)))");
    }

    @Test
    public void shouldParseTimesheetQuestionWithSingleParse() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-parser-chunking.bin").getFile());
        testRunner.setProperty(PROPERTY_NUM_PARSES, String.valueOf(1));
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList("did", "I", "report", "my", "time", "correctly", "?")));

        testRunner.enqueue("did I report my time correctly?", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_PARSER_PARSE_COUNT, String.valueOf(1));
        flowFile.assertAttributeExists(ATTRIBUTE_PARSER_PARSE_LIST);
        List<String> parsesList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_PARSER_PARSE_LIST), new TypeToken<List<String>>() {}.getType());

        assertThat(parsesList).containsExactly("(TOP (SQ (VBD did) (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time) (RB correctly))) (. ?)))");
    }

    @Test
    public void shouldParseTimesheetQuestionWithTenParses() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-parser-chunking.bin").getFile());
        testRunner.setProperty(PROPERTY_NUM_PARSES, String.valueOf(10));
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(Arrays.asList("did", "I", "report", "my", "time", "correctly", "?")));

        testRunner.enqueue("did I report my time correctly?", attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_PARSER_PARSE_COUNT, String.valueOf(10));
        flowFile.assertAttributeExists(ATTRIBUTE_PARSER_PARSE_LIST);
        List<String> parsesList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_PARSER_PARSE_LIST), new TypeToken<List<String>>() {}.getType());

        assertThat(parsesList).containsExactly(
                "(TOP (VP (SQ (VBD did) (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time)))) (PP (ADVP (RB correctly))) (. ?)))",
                "(TOP (VP (VBD did) (PP (SBAR (S (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time) (RB correctly)))))) (. ?)))",
                "(TOP (VP (VBD did) (PP (SBAR (S (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time) (RB correctly)))))) (. ?)))",
                "(TOP (VP (SQ (VBD did) (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time)))) (ADVP (RB correctly)) (. ?)))",
                "(TOP (VP (VBD did) (PP (SBAR (S (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time) (RB correctly)))))) (. ?)))",
                "(TOP (VP (VBD did) (ADVP (NP (PRP I)) (SBAR (S (VP (VB report) (NP (PRP$ my) (NN time) (RB correctly)))))) (. ?)))",
                "(TOP (S (S (VP (VBD did))) (S (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time)))) (ADVP (RB correctly)) (. ?)))",
                "(TOP (VP (VBD did) (SBAR (S (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time)) (ADVP (RB correctly))))) (. ?)))",
                "(TOP (VP (VBD did) (SBAR (S (NP (PRP I)) (VP (VB report) (NP (PRP$ my) (NN time) (RB correctly))))) (. ?)))",
                "(TOP (SQ (VBD did) (NP (PRP I)) (VP (VB report) (FRAG (NP (PRP$ my) (NN time)) (PP (ADVP (RB correctly))))) (. ?)))");
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-parser-chunking.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }
}
