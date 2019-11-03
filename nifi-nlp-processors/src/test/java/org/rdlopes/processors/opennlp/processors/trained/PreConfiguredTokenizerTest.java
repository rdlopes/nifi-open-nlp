package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.processors.opennlp.common.TokenizerType;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_SPAN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TOKENIZE_TOKENIZER_TYPE;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreConfiguredTokenizerTest extends PreTrainedProcessorTest<PreConfiguredTokenizer> {

    public PreConfiguredTokenizerTest() {
        super(PreConfiguredTokenizer.class);
    }

    @Test
    public void shouldTokenizeWithSimpleTokenizer() {
        testRunner.setProperty(TOKENIZE_TOKENIZER_TYPE.descriptor, TokenizerType.SIMPLE.name());
        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(TOKENIZE_TOKEN_LIST.key);
        flowFile.assertAttributeExists(TOKENIZE_SPAN_LIST.key);

        List<String> tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> tokenSpans = TOKENIZE_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(tokensList).containsExactly(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive", "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman",
                "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former", "chairman", "of", "Consolidated",
                "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", "."
        );
        assertThat(tokenSpans).containsExactly(
                new Span(0, 6, null), new Span(7, 13, null), new Span(14, 15, null), new Span(16, 18, null), new Span(19, 24, null),
                new Span(25, 28, null), new Span(29, 30, null), new Span(31, 35, null), new Span(36, 40, null), new Span(41, 44, null),
                new Span(45, 50, null), new Span(51, 53, null), new Span(54, 55, null), new Span(56, 68, null), new Span(69, 77, null),
                new Span(78, 81, null), new Span(81, 82, null), new Span(83, 85, null), new Span(86, 87, null), new Span(88, 90, null),
                new Span(90, 91, null), new Span(92, 98, null), new Span(99, 101, null), new Span(102, 110, null),
                new Span(111, 113, null), new Span(114, 122, null), new Span(123, 124, null), new Span(124, 125, null),
                new Span(125, 126, null), new Span(126, 127, null), new Span(128, 129, null), new Span(130, 133, null),
                new Span(134, 139, null), new Span(140, 150, null), new Span(151, 156, null), new Span(157, 158, null),
                new Span(159, 166, null), new Span(167, 172, null), new Span(173, 174, null), new Span(175, 177, null),
                new Span(178, 183, null), new Span(184, 187, null), new Span(188, 191, null), new Span(192, 198, null),
                new Span(199, 207, null), new Span(208, 210, null), new Span(211, 223, null), new Span(224, 228, null),
                new Span(229, 235, null), new Span(236, 239, null), new Span(240, 241, null), new Span(242, 245, null),
                new Span(246, 251, null), new Span(256, 257, null), new Span(258, 266, null), new Span(267, 269, null),
                new Span(270, 274, null), new Span(275, 282, null), new Span(283, 293, null), new Span(294, 306, null),
                new Span(307, 308, null)
        );
    }

    @Test
    public void shouldTokenizeWithWhitespaceTokenizer() {
        testRunner.setProperty(TOKENIZE_TOKENIZER_TYPE.descriptor, TokenizerType.WHITESPACE.name());
        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(TOKENIZE_TOKEN_LIST.key);
        flowFile.assertAttributeExists(TOKENIZE_SPAN_LIST.key);

        List<String> tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> tokenSpans = TOKENIZE_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(tokensList).containsExactly(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive", "director", "Nov.", "29", ".", "Mr.", "Vinken", "is", "chairman", "of",
                "Elsevier", "N.V.", ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former", "chairman", "of", "Consolidated", "Gold", "Fields",
                "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", ".");
        assertThat(tokenSpans).containsExactly(
                new Span(0, 6, null), new Span(7, 13, null), new Span(14, 15, null), new Span(16, 18, null), new Span(19, 24, null),
                new Span(25, 28, null), new Span(29, 30, null), new Span(31, 35, null), new Span(36, 40, null), new Span(41, 44, null),
                new Span(45, 50, null), new Span(51, 53, null), new Span(54, 55, null), new Span(56, 68, null), new Span(69, 77, null),
                new Span(78, 82, null), new Span(83, 85, null), new Span(86, 87, null), new Span(88, 91, null), new Span(92, 98, null),
                new Span(99, 101, null), new Span(102, 110, null), new Span(111, 113, null), new Span(114, 122, null),
                new Span(123, 127, null), new Span(128, 129, null), new Span(130, 133, null), new Span(134, 139, null),
                new Span(140, 150, null), new Span(151, 156, null), new Span(157, 158, null), new Span(159, 166, null),
                new Span(167, 172, null), new Span(173, 174, null), new Span(175, 177, null), new Span(178, 183, null),
                new Span(184, 187, null), new Span(188, 191, null), new Span(192, 198, null), new Span(199, 207, null),
                new Span(208, 210, null), new Span(211, 223, null), new Span(224, 228, null), new Span(229, 235, null),
                new Span(236, 239, null), new Span(240, 241, null), new Span(242, 245, null), new Span(246, 251, null),
                new Span(256, 257, null), new Span(258, 266, null), new Span(267, 269, null), new Span(270, 274, null),
                new Span(275, 282, null), new Span(283, 293, null), new Span(294, 306, null), new Span(307, 308, null));
    }

}
