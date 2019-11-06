package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.processors.opennlp.common.TokenizerType;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
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
        testRunner.enqueue(SAMPLE_CONTENT);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(TOKENIZER_TOKENS_LIST_KEY);
        flowFile.assertAttributeExists(TOKENIZER_TOKENS_SPAN_KEY);

        List<String> tokensList = get(TOKENIZER_TOKENS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> tokenSpans = get(TOKENIZER_TOKENS_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(tokensList).containsExactly(
                "==", "Please", "notice", "that", "this", "announcement", "will", "be", "updated", "at", "10", ":", "30", "AM", ",", "3", ":", "00", "PM", "and", "7", ":", "00", "PM", "==", "Pierre",
                "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "non", "-", "executive", "director", "Nov", ".", "29", "th", ".", "Mr", ".", "Vinken", "is",
                "chairman", "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch", "publishing", "group", "that", "owns", "40", "%", "of", "published", "magazines", "in", "the", "Netherlands",
                "and", "10", "%", "in", "Belgium", ".", "Elsevier", "N", ".", "V", ".", "now", "represents", "51", "%", "of", "the", "total", "capital", "of", "the", "company", ",", "worth", "more",
                "than", "$", "800", ".", "000", "(", "1", ".", "000", ".", "000", "euros", ",", "900", ".", "000", "pounds", ")", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
                "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", ".");
        assertThat(tokenSpans).containsExactly(
                new Span(0, 2, null), new Span(3, 9, null), new Span(10, 16, null), new Span(17, 21, null), new Span(22, 26, null),
                new Span(27, 39, null), new Span(40, 44, null), new Span(45, 47, null), new Span(48, 55, null), new Span(56, 58, null),
                new Span(59, 61, null), new Span(61, 62, null), new Span(62, 64, null), new Span(65, 67, null), new Span(67, 68, null),
                new Span(69, 70, null), new Span(70, 71, null), new Span(71, 73, null), new Span(74, 76, null), new Span(77, 80, null),
                new Span(81, 82, null), new Span(82, 83, null), new Span(83, 85, null), new Span(86, 88, null), new Span(89, 91, null),
                new Span(93, 99, null), new Span(100, 106, null), new Span(106, 107, null), new Span(108, 110, null), new Span(111, 116, null),
                new Span(117, 120, null), new Span(120, 121, null), new Span(122, 126, null), new Span(127, 131, null),
                new Span(132, 135, null), new Span(136, 141, null), new Span(142, 144, null), new Span(145, 146, null),
                new Span(147, 150, null), new Span(150, 151, null), new Span(151, 160, null), new Span(161, 169, null),
                new Span(170, 173, null), new Span(173, 174, null), new Span(175, 177, null), new Span(177, 179, null),
                new Span(179, 180, null), new Span(181, 183, null), new Span(183, 184, null), new Span(185, 191, null),
                new Span(192, 194, null), new Span(195, 203, null), new Span(204, 206, null), new Span(207, 215, null),
                new Span(216, 217, null), new Span(217, 218, null), new Span(218, 219, null), new Span(219, 220, null),
                new Span(220, 221, null), new Span(222, 225, null), new Span(226, 231, null), new Span(232, 242, null),
                new Span(243, 248, null), new Span(249, 253, null), new Span(254, 258, null), new Span(259, 261, null),
                new Span(261, 262, null), new Span(263, 265, null), new Span(266, 275, null), new Span(276, 285, null),
                new Span(286, 288, null), new Span(289, 292, null), new Span(293, 304, null), new Span(305, 308, null),
                new Span(309, 311, null), new Span(311, 312, null), new Span(313, 315, null), new Span(316, 323, null),
                new Span(323, 324, null), new Span(325, 333, null), new Span(334, 335, null), new Span(335, 336, null),
                new Span(336, 337, null), new Span(337, 338, null), new Span(339, 342, null), new Span(343, 353, null),
                new Span(354, 356, null), new Span(356, 357, null), new Span(358, 360, null), new Span(361, 364, null),
                new Span(365, 370, null), new Span(371, 378, null), new Span(379, 381, null), new Span(382, 385, null),
                new Span(386, 393, null), new Span(393, 394, null), new Span(395, 400, null), new Span(401, 405, null),
                new Span(406, 410, null), new Span(411, 412, null), new Span(412, 415, null), new Span(415, 416, null),
                new Span(416, 419, null), new Span(420, 421, null), new Span(422, 423, null), new Span(423, 424, null),
                new Span(424, 427, null), new Span(427, 428, null), new Span(428, 431, null), new Span(432, 437, null),
                new Span(437, 438, null), new Span(439, 442, null), new Span(442, 443, null), new Span(443, 446, null),
                new Span(447, 453, null), new Span(454, 455, null), new Span(456, 457, null), new Span(458, 465, null),
                new Span(466, 471, null), new Span(471, 472, null), new Span(473, 475, null), new Span(476, 481, null),
                new Span(482, 485, null), new Span(486, 489, null), new Span(490, 496, null), new Span(497, 505, null),
                new Span(506, 508, null), new Span(509, 521, null), new Span(522, 526, null), new Span(527, 533, null),
                new Span(534, 537, null), new Span(537, 538, null), new Span(539, 542, null), new Span(543, 548, null),
                new Span(549, 550, null), new Span(551, 559, null), new Span(560, 562, null), new Span(563, 567, null),
                new Span(568, 575, null), new Span(576, 586, null), new Span(587, 599, null), new Span(599, 600, null));
    }

    @Test
    public void shouldTokenizeWithWhitespaceTokenizer() {
        testRunner.setProperty(TOKENIZE_TOKENIZER_TYPE.descriptor, TokenizerType.WHITESPACE.name());
        testRunner.enqueue(SAMPLE_CONTENT);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(TOKENIZER_TOKENS_LIST_KEY);
        List<String> tokensList = get(TOKENIZER_TOKENS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        flowFile.assertAttributeExists(TOKENIZER_TOKENS_SPAN_KEY);
        List<Span> tokenSpans = get(TOKENIZER_TOKENS_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(tokensList).containsExactly(
                "==", "Please", "notice", "that", "this", "announcement", "will", "be", "updated", "at", "10:30", "AM,", "3:00", "PM", "and", "7:00", "PM", "==", "Pierre", "Vinken,", "61", "years",
                "old,", "will", "join", "the", "board", "as", "a", "non-executive", "director", "Nov.", "29th.", "Mr.", "Vinken", "is", "chairman", "of", "Elsevier", "N.V.,", "the", "Dutch",
                "publishing", "group", "that", "owns", "40%", "of", "published", "magazines", "in", "the", "Netherlands", "and", "10%", "in", "Belgium.", "Elsevier", "N.V.", "now", "represents",
                "51%", "of", "the", "total", "capital", "of", "the", "company,", "worth", "more", "than", "$800.000", "(", "1.000.000", "euros,", "900.000", "pounds", ")", ".", "Rudolph", "Agnew,",
                "55", "years", "old", "and", "former", "chairman", "of", "Consolidated", "Gold", "Fields", "PLC,", "was", "named", "a", "director", "of", "this", "British", "industrial",
                "conglomerate.");
        assertThat(tokenSpans).containsExactly(
                new Span(0, 2, null), new Span(3, 9, null), new Span(10, 16, null), new Span(17, 21, null), new Span(22, 26, null),
                new Span(27, 39, null), new Span(40, 44, null), new Span(45, 47, null), new Span(48, 55, null), new Span(56, 58, null),
                new Span(59, 64, null), new Span(65, 68, null), new Span(69, 73, null), new Span(74, 76, null), new Span(77, 80, null),
                new Span(81, 85, null), new Span(86, 88, null), new Span(89, 91, null), new Span(93, 99, null), new Span(100, 107, null),
                new Span(108, 110, null), new Span(111, 116, null), new Span(117, 121, null), new Span(122, 126, null),
                new Span(127, 131, null), new Span(132, 135, null), new Span(136, 141, null), new Span(142, 144, null),
                new Span(145, 146, null), new Span(147, 160, null), new Span(161, 169, null), new Span(170, 174, null),
                new Span(175, 180, null), new Span(181, 184, null), new Span(185, 191, null), new Span(192, 194, null),
                new Span(195, 203, null), new Span(204, 206, null), new Span(207, 215, null), new Span(216, 221, null),
                new Span(222, 225, null), new Span(226, 231, null), new Span(232, 242, null), new Span(243, 248, null),
                new Span(249, 253, null), new Span(254, 258, null), new Span(259, 262, null), new Span(263, 265, null),
                new Span(266, 275, null), new Span(276, 285, null), new Span(286, 288, null), new Span(289, 292, null),
                new Span(293, 304, null), new Span(305, 308, null), new Span(309, 312, null), new Span(313, 315, null),
                new Span(316, 324, null), new Span(325, 333, null), new Span(334, 338, null), new Span(339, 342, null),
                new Span(343, 353, null), new Span(354, 357, null), new Span(358, 360, null), new Span(361, 364, null),
                new Span(365, 370, null), new Span(371, 378, null), new Span(379, 381, null), new Span(382, 385, null),
                new Span(386, 394, null), new Span(395, 400, null), new Span(401, 405, null), new Span(406, 410, null),
                new Span(411, 419, null), new Span(420, 421, null), new Span(422, 431, null), new Span(432, 438, null),
                new Span(439, 446, null), new Span(447, 453, null), new Span(454, 455, null), new Span(456, 457, null),
                new Span(458, 465, null), new Span(466, 472, null), new Span(473, 475, null), new Span(476, 481, null),
                new Span(482, 485, null), new Span(486, 489, null), new Span(490, 496, null), new Span(497, 505, null),
                new Span(506, 508, null), new Span(509, 521, null), new Span(522, 526, null), new Span(527, 533, null),
                new Span(534, 538, null), new Span(539, 542, null), new Span(543, 548, null), new Span(549, 550, null),
                new Span(551, 559, null), new Span(560, 562, null), new Span(563, 567, null), new Span(568, 575, null),
                new Span(576, 586, null), new Span(587, 600, null));
    }

}
