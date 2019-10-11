package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.parser.*;
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
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static opennlp.tools.parser.ParserType.CHUNKING;
import static opennlp.tools.parser.ParserType.TREEINSERT;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.apache.nifi.processor.util.StandardValidators.*;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR_DESCRIPTION;
import static org.rdlopes.processors.opennlp.Parse.ATTRIBUTE_PARSER_PARSE_COUNT;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION;

@NlpProcessor
@Tags({"apache", "nlp", "parser"})
@CapabilityDescription("Parses the content of a flow file.")
@ReadsAttributes({@ReadsAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_LIST,
                                  description = ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR,
                                    description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_PARSER_PARSE_COUNT,
                                    description = "The number of parses evaluated, eg. the 'Top parses list size' parameter, or fewer.")})
@EqualsAndHashCode(callSuper = true)
public class Parse extends AbstractNlpProcessor<ParserModel> {

    public static final String ATTRIBUTE_PARSER_PARSE_COUNT = "nlp.parser.parse.count";

    public static final String ATTRIBUTE_PARSER_PARSE_LIST = "nlp.parser.parse.list";

    public static final PropertyDescriptor PROPERTY_ADVANCE_PERCENTAGE = new PropertyDescriptor.Builder()
            .name("Advance percentage")
            .description("Advance percentage for parser setup, as a float number between 0 and 1.")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NUMBER_VALIDATOR)
            .defaultValue(String.valueOf(AbstractBottomUpParser.defaultAdvancePercentage))
            .build();

    public static final PropertyDescriptor PROPERTY_BEAM_SIZE = new PropertyDescriptor.Builder()
            .name("Beam size")
            .description("Beam size for parser setup, as an integer.")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue(String.valueOf(AbstractBottomUpParser.defaultBeamSize))
            .build();

    public static final PropertyDescriptor PROPERTY_HEAD_RULES_FILE_PATH = new PropertyDescriptor.Builder()
            .name("Head rules file path")
            .description("Head rules file path for training the model " +
                         "(only required if training data or training file are set).")
            .required(false)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue("${NIFI_HOME}/models/en-head_rules")
            .build();

    public static final PropertyDescriptor PROPERTY_NUM_PARSES = new PropertyDescriptor.Builder()
            .name("Top parses list size")
            .description("The number of parses that the tool should evaluate (1-10).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(INTEGER_VALIDATOR)
            .defaultValue("1")
            .build();

    public static final PropertyDescriptor PROPERTY_PARSER_TYPE = new PropertyDescriptor.Builder()
            .name("Parser type")
            .description("The type of parser to use.")
            .required(true)
            .allowableValues(ParserType.values())
            .defaultValue(CHUNKING.name())
            .build();

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream
            .concat(Stream.of(PROPERTY_TRAINING_LANGUAGE,
                              PROPERTY_PARSER_TYPE,
                              PROPERTY_HEAD_RULES_FILE_PATH,
                              PROPERTY_NUM_PARSES,
                              PROPERTY_BEAM_SIZE,
                              PROPERTY_ADVANCE_PERCENTAGE),
                    super.getSupportedPropertyDescriptors().stream())
            .collect(toList());

    public Parse() {super(ParserModel.class);}

    private opennlp.tools.parser.Parse createParseSource(List<String> tokensList) {
        String aggregatedContent = join(" ", tokensList);
        opennlp.tools.parser.Parse p = new opennlp.tools.parser.Parse(aggregatedContent,
                                                                      new Span(0, aggregatedContent.length()),
                                                                      AbstractBottomUpParser.INC_NODE,
                                                                      0,
                                                                      0);
        int start = 0;
        int i = 0;
        for (Iterator<String> ti = tokensList.iterator(); ti.hasNext(); i++) {
            String tok = ti.next();
            p.insert(new opennlp.tools.parser.Parse(aggregatedContent, new Span(start, start + tok.length()), AbstractBottomUpParser.TOK_NODE, 0, i));
            start += tok.length() + 1;
        }

        return p;
    }

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, ProcessSession session, String content, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();

        int numParses = context.getProperty(PROPERTY_NUM_PARSES)
                               .evaluateAttributeExpressions()
                               .asInteger();
        int beamSize = context.getProperty(PROPERTY_BEAM_SIZE)
                              .evaluateAttributeExpressions()
                              .asInteger();
        double advancePercentage = context.getProperty(PROPERTY_ADVANCE_PERCENTAGE)
                                          .evaluateAttributeExpressions()
                                          .asDouble();
        List<String> tokensList = Arrays.asList(attributeAsStringArray(attributes.get(ATTRIBUTE_TOKENIZE_TOKEN_LIST)));

        Parser parser = ParserFactory.create(getModel(), beamSize, advancePercentage);
        final opennlp.tools.parser.Parse[] parses = parser.parse(createParseSource(tokensList), numParses);

        final List<String> parseCollection = Arrays.stream(parses).map(parse -> {
            StringBuffer builder = new StringBuffer();
            parse.show(builder);
            return builder.toString();
        }).collect(toList());

        evaluation.put(ATTRIBUTE_PARSER_PARSE_COUNT, String.valueOf(parses.length));
        evaluation.put(ATTRIBUTE_PARSER_PARSE_LIST, new Gson().toJson(parseCollection));

        return evaluation;
    }

    @Override
    protected ParserModel doTrain(ValidationContext context, TrainingParameters parameters, Charset charset, ObjectStream<String> stream) throws IOException {
        final String trainingLanguage = context.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        final ParserType parserType = ParserType.valueOf(context.getProperty(PROPERTY_PARSER_TYPE).getValue());
        final String headRulesFilePath = context.getProperty(PROPERTY_HEAD_RULES_FILE_PATH).evaluateAttributeExpressions().getValue();
        final HeadRules rules = new opennlp.tools.parser.lang.en.HeadRules(new InputStreamReader(getClass().getResourceAsStream(headRulesFilePath)));
        try (ObjectStream<opennlp.tools.parser.Parse> sampleStream = new ParseSampleStream(stream)) {
            return parserType == TREEINSERT ? opennlp.tools.parser.treeinsert.Parser.train(trainingLanguage, sampleStream, rules, parameters)
                                            : opennlp.tools.parser.chunking.Parser.train(trainingLanguage, sampleStream, rules, parameters);
        }
    }
}
