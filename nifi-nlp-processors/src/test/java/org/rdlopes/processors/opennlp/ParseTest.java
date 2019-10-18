package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.cmdline.parser.ParserTrainerTool;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.Parse.*;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;

public class ParseTest extends AbstractNlpProcessorTest {
    public ParseTest() {
        super(Parse.class, true);
    }

    @Test
    public void apacheOpenNLPShouldWorkAsDocumented() {
        new ParserTrainerTool().run("opennlp", new String[]{
                "-model", "target/test-classes/store/en-parser-chunking.bin",
                "-parserType", "CHUNKING",
                "-headRules", getClass().getResource("/models/en-head_rules").getFile(),
                "-lang", "en",
                "-data", getClass().getResource("/training/parser.train").getFile(),
                "-encoding", "ISO-8859-1"});
        assertThat(Paths.get(getClass().getResource("/store/en-parser-chunking.bin").getFile()))
                .exists();
    }

    @Test
    public void shouldParseOpenNLPExampleWithSingleParse() {
        testRunner.setProperty(PROPERTY_TRAINING_FILE_PATH, getClass().getResource("/training/parser.train").getFile());
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

        assertThat(parsesList).containsExactly(
                "(TOP (S (S (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (NP (CD 61) (NNS years) (VBD old))) (, ,) (VP (MD will) (PP (IN join) (NP (DT the) (NN board))) (PP (IN as) (NP (DT a) (NN" +
                " nonexecutive) (NN director))) (NP (NNP Nov)))) (. .) (NP (NP (CD 29)) (. .) (NP (NNP Mr))) (. .) ('' Vinken) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Elsevier) " +
                "(NNP N))) (. .) (NP (PRP V))))) (. .) (, ,) (NP (DT the) (NNP Dutch) (NN publishing) (NN group)) (. .) (VP (-RRB- Rudolph) (NP (NNP Agnew)))) (, ,) (NP (CD 55) (NNS years)) (VP " +
                "(VBD old) (CC and) (VP (PP (S (PP (IN former) (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC))))) (, ,) (VP (VBD was) (VP (VBN named) " +
                "(NP (NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (NNP British) (NN industrial) (NN conglomerate)))))))))) (. .)))",

                "(TOP (S (S (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (NP (CD 61) (NNS years) (VBD old))) (, ,) (VP (MD will) (VP (IN join) (NP (DT the) (NN board)) (PP (IN as) (NP (DT a) (NN " +
                "nonexecutive) (NN director))) (NP (NNP Nov))))) (. .) (NP (NP (CD 29)) (. .) (NP (NNP Mr))) (. .) ('' Vinken) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Elsevier) " +
                "(NNP N))) (. .) (NP (PRP V))))) (. .) (, ,) (NP (DT the) (NNP Dutch) (NN publishing) (NN group)) (. .) (VP (-RRB- Rudolph) (NP (NNP Agnew)))) (, ,) (NP (CD 55) (NNS years)) (VP " +
                "(VBD old) (CC and) (VP (PP (S (PP (IN former) (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC))))) (, ,) (VP (VBD was) (VP (VBN named) " +
                "(NP (NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (NNP British) (NN industrial) (NN conglomerate)))))))))) (. .)))",

                "(TOP (S (S (S (S (NP (NP (NNP Pierre) (NNP Vinken)) (, ,) (NP (CD 61) (NNS years) (VBD old))) (, ,) (VP (MD will) (PP (IN join) (NP (DT the) (NN board))) (PP (IN as) (NP (DT a) (NN" +
                " nonexecutive) (NN director))) (NP (NNP Nov)))) (. .) (NP (NP (CD 29)) (. .) (NP (NNP Mr))) (. .) ('' Vinken) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Elsevier) " +
                "(NNP N))) (. .) (NP (PRP V))))) (. .) (, ,) (NP (DT the) (NNP Dutch) (NN publishing) (NN group)) (. .) (VP (-RRB- Rudolph) (NP (NNP Agnew)))) (, ,) (NP (CD 55) (NNS years)) (VP " +
                "(VBD old) (CC and) (PP (S (PP (IN former) (NP (NP (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC))))) (, ,) (VP (VBD was) (VP (VBN named) (NP " +
                "(NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (NNP British) (NN industrial) (NN conglomerate))))))))) (. .)))");
    }

    @Test
    public void shouldParseTimesheetQuestionWithSingleParse() {
        testRunner.setProperty(PROPERTY_TRAINING_FILE_PATH, getClass().getResource("/training/parser.train").getFile());
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

        assertThat(parsesList).containsExactly("(TOP (S (VBD did) (VP (NP (PRP I)) (NP (RB report) (JJ my) (NN time) (NN correctly))) (. ?)))");
    }

    @Test
    public void shouldParseTimesheetQuestionWithTenParses() {
        testRunner.setProperty(PROPERTY_TRAINING_FILE_PATH, getClass().getResource("/training/parser.train").getFile());
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
                "(TOP (S (VBD did) (PP (VP (PRP I) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (. ?)))",
                "(TOP (S (NN did) (PP (VP (PRP I) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (. ?)))",
                "(TOP (S (NN did) (PP (VP (PRP I) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (. ?)))",
                "(TOP (S (VBD did) (PP (VP (NP (PRP I)) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (. ?)))",
                "(TOP (S (VBD did) (PP (VP (NP (PRP I)) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (. ?)))",
                "(TOP (S (NN did) (PP (VP (PRP I) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (, ?)))",
                "(TOP (S (NN did) (PP (VP (PRP I) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (, ?)))",
                "(TOP (S (VBD did) (PP (VP (NP (PRP I)) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (, ?)))",
                "(TOP (S (VBD did) (PP (VP (NP (PRP I)) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (, ?)))",
                "(TOP (S (VBD did) (VP (NP (NP (PRP I)) (RB report) (NP (JJ my) (NN time) (NN correctly)))) (. ?)))");
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_TRAINING_FILE_PATH, getClass().getResource("/training/parser.train").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }
}
