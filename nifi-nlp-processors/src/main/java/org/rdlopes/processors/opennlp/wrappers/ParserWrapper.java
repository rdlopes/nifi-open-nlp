package org.rdlopes.processors.opennlp.wrappers;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.parser.*;
import opennlp.tools.parser.lang.es.AncoraSpanishHeadRules;
import opennlp.tools.util.*;
import opennlp.tools.util.model.ArtifactSerializer;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.stream.Collectors.toList;
import static opennlp.tools.parser.ParserType.TREEINSERT;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.PARSER_PARSE_LIST;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

public class ParserWrapper extends NLPToolWrapper<Parser, ParserModel> {

    public ParserWrapper() {
        super(ParserModel.class);
    }

    private HeadRules createHeadRules(String trainingLanguage, String headRulesFilePath) throws IOException {
        ArtifactSerializer<? extends HeadRules> headRulesSerializer = "es".equals(trainingLanguage) ?
                                                                      new AncoraSpanishHeadRules.HeadRulesSerializer() :
                                                                      new opennlp.tools.parser.lang.en.HeadRules.HeadRulesSerializer();
        return headRulesSerializer.create(Files.newInputStream(Paths.get(headRulesFilePath), READ));
    }

    private Parse createParseSource(List<String> tokensList) {
        String aggregatedContent = join(" ", tokensList);
        Parse p = new Parse(aggregatedContent,
                            new Span(0, aggregatedContent.length()),
                            AbstractBottomUpParser.INC_NODE,
                            0,
                            0);
        int start = 0;
        int i = 0;
        for (Iterator<String> ti = tokensList.iterator(); ti.hasNext(); i++) {
            String tok = ti.next();
            p.insert(new Parse(aggregatedContent, new Span(start, start + tok.length()), AbstractBottomUpParser.TOK_NODE, 0, i));
            start += tok.length() + 1;
        }

        return p;
    }

    @Override
    public void evaluateContent(ProcessContext context, ParserModel model, String content, Map<String, String> attributes) {
        int numParses = PARSER_PARSES_COUNT.getIntFrom(context);
        int beamSize = PARSER_BEAM_SIZE.getIntFrom(context);
        double advancePercentage = PARSER_ADVANCE_PERCENTAGE.getDoubleFrom(context);
        String[] tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});

        Parser parser = ParserFactory.create(model, beamSize, advancePercentage);
        Parse[] parses = parser.parse(createParseSource(Arrays.asList(tokensList)), numParses);

        final List<String> parseCollection = Arrays.stream(parses).map(parse -> {
            StringBuffer builder = new StringBuffer();
            parse.show(builder);
            return builder.toString();
        }).collect(toList());

        PARSER_PARSE_LIST.updateAttributesWithJson(attributes, parseCollection);
    }

    @Override
    public ParserModel trainModel(PropertyContext propertyContext,
                                  String trainingLanguage,
                                  Charset charset,
                                  TrainingParameters trainingParameters,
                                  InputStreamFactory inputStreamFactory) throws IOException {
        final ParserType parserType = PARSER_PARSER_TYPE.getEnumFrom(propertyContext, ParserType.class);
        final String headRulesFilePath = PARSER_HEAD_RULES_FILE_PATH.getStringFrom(propertyContext);
        final HeadRules headRules = createHeadRules(trainingLanguage, headRulesFilePath);

        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ParseSampleStream sampleStream = new ParseSampleStream(lineStream)) {
            return parserType == TREEINSERT ? opennlp.tools.parser.treeinsert.Parser.train(trainingLanguage, sampleStream, headRules, trainingParameters)
                                            : opennlp.tools.parser.chunking.Parser.train(trainingLanguage, sampleStream, headRules, trainingParameters);
        }
    }
}
