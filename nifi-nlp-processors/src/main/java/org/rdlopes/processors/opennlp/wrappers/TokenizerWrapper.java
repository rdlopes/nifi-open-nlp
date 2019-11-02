package org.rdlopes.processors.opennlp.wrappers;

import opennlp.tools.tokenize.*;
import opennlp.tools.util.*;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.processors.opennlp.common.TokenizerType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_SPAN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TOKENIZE_TOKENIZER_TYPE;

public class TokenizerWrapper extends NLPToolWrapper<TokenizerModel> {

    private static final Pattern untokenizedParenthesisPattern1 = Pattern.compile("([^ ])([({)}])");

    private static final Pattern untokenizedParenthesisPattern2 = Pattern.compile("([({)}])([^ ])");

    public TokenizerWrapper() {
        super(TokenizerModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, TokenizerModel model, String content, Map<String, String> attributes) {
        final TokenizerType tokenizerType = TOKENIZE_TOKENIZER_TYPE.getEnumFrom(context, TokenizerType.class);
        final Tokenizer tokenizer = tokenizerType.tokenizer;

        String normalizedContent = normalizeTokenizedContent(content);
        String[] tokensList = tokenizer.tokenize(normalizedContent);
        Span[] tokensAsSpans = tokenizer.tokenizePos(normalizedContent);

        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, tokensList);
        TOKENIZE_SPAN_LIST.updateAttributesWithJson(attributes, tokensAsSpans);
    }

    @Override
    public TokenizerModel trainModel(PropertyContext propertyContext,
                                     String trainingLanguage,
                                     Charset charset,
                                     TrainingParameters trainingParameters,
                                     InputStreamFactory inputStreamFactory) throws IOException {
        TokenizerFactory factory = TokenizerFactory.create(null, trainingLanguage, null, true, null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<TokenSample> sampleStream = new TokenSampleStream(lineStream)) {
            return TokenizerME.train(sampleStream, factory, trainingParameters);
        }
    }

    private String normalizeTokenizedContent(String content) {
        String normalizedContent = untokenizedParenthesisPattern1.matcher(content).replaceAll("$1 $2");
        return untokenizedParenthesisPattern2.matcher(normalizedContent).replaceAll("$1 $2");
    }

}
