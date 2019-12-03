package org.rdlopes.opennlp.tools;

import opennlp.tools.tokenize.*;
import opennlp.tools.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.exception.ProcessException;
import org.rdlopes.opennlp.common.NLPAttribute;
import org.rdlopes.opennlp.common.NLPProperty;
import org.rdlopes.opennlp.common.TokenizerType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TokenizerTool extends NLPTool<TokenizerModel> {
    private static final Pattern untokenizedParenthesisPattern1 = Pattern.compile("([^ ])([({)}])");

    private static final Pattern untokenizedParenthesisPattern2 = Pattern.compile("([({)}])([^ ])");

    public TokenizerTool(Path modelPath, ComponentLog logger) {
        super(TokenizerModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, TokenizerModel model, Map<String, String> evaluation)
            throws IOException {

        Tokenizer tokenizer = NLPProperty.TOKENIZE_TOKENIZER_TYPE.isSetIn(processContext)
                ? NLPProperty.TOKENIZE_TOKENIZER_TYPE.getEnumFrom(processContext, TokenizerType.class).tokenizer
                : new TokenizerME(model);

        String contentString = IOUtils.toString(content, charset);
        String normalizedContent = normalizeTokenizedContent(contentString);
        String[] tokensList = tokenizer.tokenize(normalizedContent);
        Span[] tokensAsSpans = tokenizer.tokenizePos(normalizedContent);

        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, evaluation, tokensList);
        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_SPAN_KEY, evaluation, tokensAsSpans);
    }

    @Override
    public Map<String, String> processContent(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes) {
        if (NLPProperty.TOKENIZE_TOKENIZER_TYPE.isSetIn(processContext)) {
            Map<String, String> evaluation = new HashMap<>();
            try {
                evaluate(processContext, content, charset, attributes, null, evaluation);
            } catch (Exception e) {
                throw new ProcessException("Processing content failed", e);
            }
            return evaluation;
        } else {
            return super.processContent(processContext, content, charset, attributes);
        }
    }

    @Override
    protected TokenizerModel trainModel(ValidationContext validationContext, InputStreamFactory inputStreamFactory, TrainingParameters trainingParameters, String trainingLanguage) throws IOException {
        TokenizerFactory factory = TokenizerFactory.create(null, trainingLanguage, null, true, null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ObjectStream<TokenSample> sampleStream = new TokenSampleStream(lineStream)) {
            return TokenizerME.train(sampleStream, factory, trainingParameters);
        }
    }

    private String normalizeTokenizedContent(String content) {
        String normalizedContent = untokenizedParenthesisPattern1.matcher(content).replaceAll("$1 $2");
        return untokenizedParenthesisPattern2.matcher(normalizedContent).replaceAll("$1 $2");
    }

}
