package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.apache.nifi.processor.util.StandardValidators.NON_BLANK_VALIDATOR;
import static org.rdlopes.processors.opennlp.DetectSentences.ATTRIBUTE_SENTDET_CHUNK_LIST;
import static org.rdlopes.processors.opennlp.FindNames.ATTRIBUTE_NAMEFIND_PROBABILITIES;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION;
import static org.rdlopes.processors.opennlp.Tokenize.*;

@NlpProcessor
@Tags({"apache", "nlp", "tokenizer"})
@CapabilityDescription("Tokenizes the content of a flow file.")
@ReadsAttributes({@ReadsAttribute(attribute = ATTRIBUTE_SENTDET_CHUNK_LIST, description = ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_COUNT, description = "The number of tokens found in the flow file."),
                   @WritesAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_LIST, description = ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_SPANS, description = "Holds  the list of token spans found in flow file content, as a JSON span list."),
                   @WritesAttribute(attribute = ATTRIBUTE_NAMEFIND_PROBABILITIES, description = "Holds probabilities for each span prediction from flow file content.")})
@EqualsAndHashCode(callSuper = true)
public class Tokenize extends AbstractNlpProcessor<TokenizerModel> {

    public static final String ATTRIBUTE_TOKENIZE_TOKEN_LIST = "nlp.tokenize.token.list";

    static final String ATTRIBUTE_TOKENIZE_TOKEN_COUNT = "nlp.tokenize.token.count";

    static final String ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION = "The list of tokens as found in the content of the flow file.";

    static final String ATTRIBUTE_TOKENIZE_TOKEN_SPANS = "nlp.tokenize.token.spans";

    static final PropertyDescriptor PROPERTY_TOKENIZER_TYPE = new PropertyDescriptor.Builder()
            .name("Tokenizer type")
            .description("Defines the tokenizer implementation to use, as defined by Apache NLP." +
                         "Most part-of-speech taggers, parsers and so on, work with text tokenized in this manner. " +
                         "It is important to ensure that your tokenizer produces tokens of the type expected by your later text processing components.")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .allowableValues(TokenizerType.values())
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue(TokenizerType.SIMPLE.name())
            .build();

    private static final Pattern untokenizedParenthesisPattern1 = Pattern.compile("([^ ])([({)}])");

    private static final Pattern untokenizedParenthesisPattern2 = Pattern.compile("([({)}])([^ ])");

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(Stream.of(PROPERTY_TOKENIZER_TYPE),
                                                                                        super.getSupportedPropertyDescriptors().stream())
                                                                                .collect(toList());

    public Tokenize() {super(TokenizerModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, String content, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();
        final TokenizerType tokenizerType = TokenizerType.valueOf(context.getProperty(PROPERTY_TOKENIZER_TYPE)
                                                                         .evaluateAttributeExpressions()
                                                                         .getValue());

        Tokenizer tokenizer;
        switch (tokenizerType) {
            case WHITESPACE:
                tokenizer = WhitespaceTokenizer.INSTANCE;
                break;
            case SIMPLE:
                tokenizer = SimpleTokenizer.INSTANCE;
                break;
            case LEARNABLE:
                tokenizer = new TokenizerME(getModel());
                break;
            default:
                throw new IllegalArgumentException("tokenizer type cannot be null");
        }

        String normalizedContent = normalizeTokenizedContent(content);
        String[] tokensList = tokenizer.tokenize(normalizedContent);
        Span[] tokensAsSpans = tokenizer.tokenizePos(normalizedContent);

        evaluation.put(ATTRIBUTE_TOKENIZE_TOKEN_COUNT, String.valueOf(tokensList.length));
        evaluation.put(ATTRIBUTE_TOKENIZE_TOKEN_LIST, new Gson().toJson(tokensList));
        evaluation.put(ATTRIBUTE_TOKENIZE_TOKEN_SPANS, new Gson().toJson(tokensAsSpans));

        return evaluation;
    }

    @Override
    protected TokenizerModel doTrain(ValidationContext context, TrainingParameters parameters, Charset charset, ObjectStream<String> stream) throws IOException {
        TokenizerFactory factory = new TokenizerFactory();
        try (ObjectStream<TokenSample> sampleStream = new TokenSampleStream(stream)) {
            return TokenizerME.train(sampleStream, factory, parameters);
        }
    }

    @Override
    protected boolean isTrainingRequired(PropertyContext context) {
        final TokenizerType tokenizerType = TokenizerType.valueOf(context.getProperty(PROPERTY_TOKENIZER_TYPE)
                                                                         .evaluateAttributeExpressions()
                                                                         .getValue());
        return TokenizerType.LEARNABLE == tokenizerType;
    }

    private String normalizeTokenizedContent(String content) {
        String normalizedContent = untokenizedParenthesisPattern1.matcher(content).replaceAll("$1 $2");
        return untokenizedParenthesisPattern2.matcher(normalizedContent).replaceAll("$1 $2");
    }

    public enum TokenizerType {WHITESPACE, SIMPLE, LEARNABLE}
}
