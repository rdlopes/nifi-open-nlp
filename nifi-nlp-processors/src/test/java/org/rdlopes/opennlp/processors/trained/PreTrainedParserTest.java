package org.rdlopes.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.NLPAttribute;
import org.rdlopes.opennlp.common.NLPProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PreTrainedParserTest extends PreTrainedProcessorTest<PreTrainedParser> {

    public PreTrainedParserTest() {
        super(PreTrainedParser.class, "/models/en-parser-chunking.bin");
    }

    @Test
    public void shouldParse() {
        testRunner.setProperty(NLPProperty.PARSER_PARSES_COUNT.descriptor, "3");
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, SAMPLE_TOKENS_SIMPLE);

        testRunner.enqueue(SAMPLE_CONTENT, attributes);
        testRunner.run();
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(BaseProcessor.RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes.get(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(NLPAttribute.PARSER_PARSES_LIST_KEY);
        List<String> parsesList = NLPAttribute.get(NLPAttribute.PARSER_PARSES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {
        });
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
