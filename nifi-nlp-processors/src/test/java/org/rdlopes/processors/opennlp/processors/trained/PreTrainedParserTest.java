package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.PARSER_PARSES_COUNT;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedParserTest extends PreTrainedProcessorTest<PreTrainedParser> {

    public PreTrainedParserTest() {
        super(PreTrainedParser.class);
    }

    @Test
    public void shouldParse() throws URISyntaxException {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-parser-chunking.bin").toString());
        testRunner.setProperty(PARSER_PARSES_COUNT.descriptor, "3");
        Map<String, String> attributes = new HashMap<>();
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, SAMPLE_TOKENS_SIMPLE);

        testRunner.enqueue(SAMPLE_CONTENT, attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZER_TOKENS_LIST_KEY, attributes.get(TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(PARSER_PARSES_LIST_KEY);
        List<String> parsesList = get(PARSER_PARSES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        assertThat(parsesList).containsExactly(
                "(TOP (S (S (S (NP (NN ==)) (VP (VB Please) (S (VP (VB notice) (SBAR (IN that) (S (NP (DT this) (NN announcement)) (VP (MD will) (VP (VB be) (VP (VBN updated) (PP (IN at) " +
                "(NP (NP (CD 10)) (: :) (S (NP (CD 30)) (VP (VBP AM) (, ,) (S (NP (NP (CD 3)) (: :) (NP (NP (NP (CD 00) (NNP PM)) (CC and) (NP (CD 7))) (: :) (S (NP (NP (CD 00) (NNP PM) " +
                "(NNP ==) (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) (NN board)) (SBAR (IN as) (S (NP (DT a) " +
                "(FW non) (: -) (NN executive) (NN director) (NNP Nov) (. .) (CD 29) (NN th) (. .) (NNP Mr) (. .) (NNP Vinken)) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NP (NNP " +
                "Elsevier) (NNP N) (NNP .) (NNP V) (NNP .)) (, ,) (NP (NP (DT the) (JJ Dutch) (NN publishing) (NN group)) (SBAR (WHNP (WDT that)) (S (VP (VBZ owns) (NP (NP (CD 40) (NN %)) " +
                "(PP (IN of) (NP (NP (NP (NP (VBN published) (NNS magazines)) (PP (IN in) (NP (DT the) (NNP Netherlands)))) (CC and) (NP (NP (CD 10) (NN %)) (PP (IN in) (NP (NNP Belgium))))) " +
                "(. .) (NNP Elsevier) (NNP N) (NNP .) (NNP V))))))))))))))))))) (. .) (ADVP (RB now)) (VP (VBZ represents) (NP (NP (CD 51) (NN %)) (PP (IN of) (NP (NP (DT the) (JJ total) (NN " +
                "capital))" +
                " (PP (IN of) (NP (DT the) (NN company))))))))))))))))))))) (, ,) (S (VP (NN worth) (NP (NP (QP (JJR more) (IN than) ($ $) (CD 800))) (. .) (CD 000) (VP (LST (-LRB- -LRB-)) (NP (LST" +
                " " +
                "(LS 1) (. .)) (NP (CD 000)) (. .) (NP (CD 000) (NNS euros)) (, ,) (NP (CD 900) (. .) (CD 000) (NNS pounds)) (-RRB- -RRB-))))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (S " +
                "(UCP" +
                " (ADJP (NP (CD 55) (NNS years)) (JJ old)) (CC and) (S (NP (NP (JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was)" +
                " " +
                "(VP (VBN named) (S (NP (NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate))))))))))) (. .)))",
                "(TOP (S (S (NP (NN ==)) (VP (VB Please) (S (VP (VB notice) (SBAR (IN that) (S (NP (DT this) (NN announcement)) (VP (MD will) (VP (VB be) (VP (VBN updated) (PP (IN at) (NP (NP (CD " +
                "10)) " +
                "(: :) (S (NP (CD 30)) (VP (VBP AM) (, ,) (S (NP (NP (CD 3)) (: :) (NP (NP (NP (CD 00) (NNP PM)) (CC and) (NP (CD 7))) (: :) (S (NP (NP (CD 00) (NNP PM) (NNP ==) (NNP Pierre) " +
                "(NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) (NN board)) (SBAR (IN as) (S (NP (DT a) (FW non) (: -) (NN executive)" +
                " (NN director) (NNP Nov) (. .) (CD 29) (NN th) (. .) (NNP Mr) (. .) (NNP Vinken)) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NP (NNP Elsevier) (NNP N) (NNP .) (NNP V) " +
                "(NNP .)) (, ,) (NP (NP (DT the) (JJ Dutch) (NN publishing) (NN group)) (SBAR (WHNP (WDT that)) (S (VP (VBZ owns) (NP (NP (CD 40) (NN %)) (PP (IN of) (NP (NP (NP (NP (VBN published)" +
                " (NNS magazines)) (PP (IN in) (NP (DT the) (NNP Netherlands)))) (CC and) (NP (NP (CD 10) (NN %)) (PP (IN in) (NP (NNP Belgium))))) (. .) (NNP Elsevier) (NNP N) (NNP .) " +
                "(NNP V))))))))))))))))))) (. .) (ADVP (RB now)) (VP (VBZ represents) (NP (NP (CD 51) (NN %)) (PP (IN of) (NP (NP (DT the) (JJ total) (NN capital)) (PP (IN of) (NP (DT the)" +
                " (NN company))))))))))))))))))))) (, ,) (S (VP (NN worth) (NP (NP (QP (JJR more) (IN than) ($ $) (CD 800))) (. .) (CD 000) (PP (VP (LST (-LRB- -LRB-)) (NP (LST (LS 1) (. .)) " +
                "(NP (CD 000)) (. .) (NP (CD 000) (NNS euros)) (, ,) (NP (CD 900) (. .) (CD 000) (NNS pounds)) (-RRB- -RRB-))))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (S (UCP (ADJP " +
                "(NP (CD 55) (NNS years)) (JJ old)) (CC and) (S (NP (NP (JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) " +
                "(VP (VBN named) (S (NP (NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate))))))))))) (. .)))",
                "(TOP (S (S (S (NP (NN ==)) (VP (VB Please) (S (VP (VB notice) (SBAR (IN that) (S (NP (DT this) (NN announcement)) (VP (MD will) (VP (VB be) (VP (VBN updated) (PP (IN at) (NP " +
                "(NP (CD 10)) (: :) (S (NP (CD 30)) (VP (VBP AM) (, ,) (S (NP (NP (CD 3)) (: :) (NP (NP (NP (CD 00) (NNP PM)) (CC and) (NP (CD 7))) (: :) (S (NP (NP (CD 00) (NNP PM) (NNP ==)" +
                " (NNP Pierre) (NNP Vinken)) (, ,) (ADJP (NP (CD 61) (NNS years)) (JJ old))) (, ,) (VP (MD will) (VP (VB join) (NP (DT the) (NN board)) (SBAR (IN as) (S (NP (DT a) (FW non) (: -)" +
                " (NN executive) (NN director) (NNP Nov) (. .) (CD 29) (NN th) (. .) (NNP Mr) (. .) (NNP Vinken)) (VP (VBZ is) (NP (NP (NN chairman)) (PP (IN of) (NP (NP (NNP Elsevier) (NNP N) " +
                "(NNP .) (NNP V) (NNP .)) (, ,) (NP (NP (DT the) (JJ Dutch) (NN publishing) (NN group)) (SBAR (WHNP (WDT that)) (S (VP (VBZ owns) (NP (NP (CD 40) (NN %)) (PP (IN of) (NP (NP (NP " +
                "(NP (VBN published) (NNS magazines)) (PP (IN in) (NP (DT the) (NNP Netherlands)))) (CC and) (NP (NP (CD 10) (NN %)) (PP (IN in) (NP (NNP Belgium))))) (. .) (NNP Elsevier) (NNP N)" +
                " (NNP .) (NNP V))))))))))))))))))) (. .) (ADVP (RB now)) (VP (VBZ represents) (NP (NP (CD 51) (NN %)) (PP (IN of) (NP (NP (DT the) (JJ total) (NN capital)) (PP (IN of) (NP (DT the)" +
                " " +
                "(NN company))))))))))))))))))))) (, ,) (S (VP (NN worth) (NP (NP (NP (QP (JJR more) (IN than) ($ $) (CD 800))) (. .) (CD 000) (VP (LST (-LRB- -LRB-)) (NP (LST (LS 1) (. .)) (NP " +
                "(CD 000))))) (. .) (NP (CD 000) (NNS euros)) (, ,) (NP (CD 900) (. .) (CD 000) (NNS pounds)) (-RRB- -RRB-))))) (. .) (NP (NP (NNP Rudolph) (NNP Agnew)) (, ,) (UCP (ADJP (NP (CD 55)" +
                " " +
                "(NNS years)) (JJ old)) (CC and) (S (NP (NP (JJ former) (NN chairman)) (PP (IN of) (NP (NNP Consolidated) (NNP Gold) (NNP Fields) (NNP PLC)))) (, ,) (VP (VBD was) (VP (VBN named) " +
                "(S (NP (NP (DT a) (NN director)) (PP (IN of) (NP (DT this) (JJ British) (JJ industrial) (NN conglomerate)))))))))) (. .)))");
    }

}
