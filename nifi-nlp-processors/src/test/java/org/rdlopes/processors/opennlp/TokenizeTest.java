package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.Tokenize.*;

public class TokenizeTest extends AbstractNlpProcessorTest {

    public TokenizeTest() {
        super(Tokenize.class, false);
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-token.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }

    @Test
    public void shouldTTokenizeSentenceWithSimpleTokenizer() {
        testRunner.setProperty(PROPERTY_TOKENIZER_TYPE, TokenizerType.SIMPLE.name());
        testRunner.enqueue("Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                           "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                           "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                           "    a director of this British industrial conglomerate .");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_TOKENIZE_TOKEN_COUNT, String.valueOf(61));
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_LIST);
        List<String> tokensList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_LIST), new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_SPANS);
        List<Span> tokenSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_SPANS),
                                                    new TypeToken<List<Span>>() {}.getType());

        assertThat(tokensList).containsExactly(
                "Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive",
                "director", "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".",
                ",", "the", "Dutch", "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
                "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this",
                "British", "industrial", "conglomerate", ".");
        assertThat(tokenSpans).containsExactly(
                new Span(0, 6), new Span(7, 13), new Span(14, 15), new Span(16, 18), new Span(19, 24), new Span(25, 28),
                new Span(29, 30), new Span(31, 35), new Span(36, 40), new Span(41, 44), new Span(45, 50), new Span(51, 53),
                new Span(54, 55), new Span(56, 68), new Span(69, 77), new Span(78, 81), new Span(81, 82), new Span(83, 85),
                new Span(86, 87), new Span(88, 90), new Span(91, 92), new Span(93, 99), new Span(100, 102), new Span(103, 111),
                new Span(112, 114), new Span(115, 123), new Span(124, 125), new Span(125, 126), new Span(126, 127),
                new Span(127, 128), new Span(129, 130), new Span(131, 134), new Span(135, 140), new Span(141, 151),
                new Span(152, 157), new Span(158, 159), new Span(160, 167), new Span(168, 173), new Span(174, 175),
                new Span(176, 178), new Span(179, 184), new Span(185, 188), new Span(189, 192), new Span(193, 199),
                new Span(200, 208), new Span(209, 211), new Span(212, 224), new Span(225, 229), new Span(230, 236),
                new Span(237, 240), new Span(241, 242), new Span(243, 246), new Span(247, 252), new Span(257, 258),
                new Span(259, 267), new Span(268, 270), new Span(271, 275), new Span(276, 283), new Span(284, 294),
                new Span(295, 307), new Span(308, 309)
        );
    }

    @Test
    public void shouldTTokenizeTimesheetQuestionWithLearnableTokenizer() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/en-token.bin").getFile());
        testRunner.setProperty(PROPERTY_TOKENIZER_TYPE, TokenizerType.LEARNABLE.name());
        testRunner.enqueue("did I report my time correctly?");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_TOKENIZE_TOKEN_COUNT, String.valueOf(7));
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_LIST);
        List<String> tokensList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_LIST), new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_SPANS);
        List<Span> tokenSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_SPANS),
                                                    new TypeToken<List<Span>>() {}.getType());

        assertThat(tokensList).containsExactly("did", "I", "report", "my", "time", "correctly", "?");
        assertThat(tokenSpans).containsExactly(new Span(0, 3),
                                               new Span(4, 5),
                                               new Span(6, 12),
                                               new Span(13, 15),
                                               new Span(16, 20),
                                               new Span(21, 30),
                                               new Span(30, 31));
    }

    @Test
    public void shouldTTokenizeTimesheetQuestionWithSimpleTokenizer() {
        testRunner.setProperty(PROPERTY_TOKENIZER_TYPE, TokenizerType.SIMPLE.name());
        testRunner.enqueue("did I report my time correctly?");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_TOKENIZE_TOKEN_COUNT, String.valueOf(7));
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_LIST);
        List<String> tokensList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_LIST), new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_SPANS);
        List<Span> tokenSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_SPANS),
                                                    new TypeToken<List<Span>>() {}.getType());

        assertThat(tokensList).containsExactly("did", "I", "report", "my", "time", "correctly", "?");
        assertThat(tokenSpans).containsExactly(new Span(0, 3),
                                               new Span(4, 5),
                                               new Span(6, 12),
                                               new Span(13, 15),
                                               new Span(16, 20),
                                               new Span(21, 30),
                                               new Span(30, 31));
    }

    @Test
    public void shouldTokenizeTimesheetQuestionWithWhitespaceTokenizer() {
        testRunner.setProperty(PROPERTY_TOKENIZER_TYPE, TokenizerType.WHITESPACE.name());
        testRunner.enqueue("did I report my time correctly?");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_TOKENIZE_TOKEN_COUNT, String.valueOf(6));
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_LIST);
        List<String> tokensList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_LIST), new TypeToken<List<String>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_TOKENIZE_TOKEN_SPANS);
        List<Span> tokenSpans = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_TOKENIZE_TOKEN_SPANS), new TypeToken<List<Span>>() {}.getType());

        assertThat(tokensList).containsExactly("did", "I", "report", "my", "time", "correctly?");
        assertThat(tokenSpans).containsExactly(new Span(0, 3),
                                               new Span(4, 5),
                                               new Span(6, 12),
                                               new Span(13, 15),
                                               new Span(16, 20),
                                               new Span(21, 31));
    }

}
