package org.rdlopes.processors.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.parser.*;
import opennlp.tools.parser.lang.es.AncoraSpanishHeadRules;
import opennlp.tools.util.*;
import opennlp.tools.util.model.ArtifactSerializer;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.stream.Collectors.toList;
import static opennlp.tools.parser.ParserType.TREEINSERT;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

public class ParserTool extends NLPTool<ParserModel> {
    public ParserTool(Path modelPath, ComponentLog logger) {
        super(ParserModel.class, modelPath, logger);
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
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, ParserModel model, Map<String, String> evaluation) {
        int numParses = PARSER_PARSES_COUNT.getIntFrom(processContext);
        int beamSize = PARSER_BEAM_SIZE.getIntFrom(processContext);
        double advancePercentage = PARSER_ADVANCE_PERCENTAGE.getDoubleFrom(processContext);
        String[] tokensList = get(TOKENIZER_TOKENS_LIST_KEY, attributes, new TypeToken<String[]>() {});

        Parser parser = ParserFactory.create(model, beamSize, advancePercentage);
        Parse[] parses = parser.parse(createParseSource(Arrays.asList(tokensList)), numParses);

        final List<String> parseCollection = Arrays.stream(parses).map(parse -> {
            StringBuffer builder = new StringBuffer();
            parse.show(builder);
            return builder.toString();
        }).collect(toList());

        set(PARSER_PARSES_LIST_KEY, evaluation, parseCollection);
    }

    @Override
    protected ParserModel trainModel(ValidationContext validationContext, InputStreamFactory inputStreamFactory, TrainingParameters trainingParameters, String trainingLanguage) throws IOException {
        final ParserType parserType = PARSER_PARSER_TYPE.getEnumFrom(validationContext, ParserType.class);
        final String headRulesFilePath = PARSER_HEAD_RULES_FILE_PATH.getStringFrom(validationContext);
        final HeadRules headRules = createHeadRules(trainingLanguage, headRulesFilePath);

        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ParseSampleStream sampleStream = new ParseSampleStream(lineStream)) {
            return parserType == TREEINSERT ? opennlp.tools.parser.treeinsert.Parser.train(trainingLanguage, sampleStream, headRules, trainingParameters)
                                            : opennlp.tools.parser.chunking.Parser.train(trainingLanguage, sampleStream, headRules, trainingParameters);
        }
    }
}
