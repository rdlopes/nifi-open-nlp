package org.rdlopes.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.parser.ParserModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.ParserProcessor;
import org.rdlopes.opennlp.tools.ParserTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.opennlp.common.NLPProperty.*;

@EqualsAndHashCode(callSuper = true)
@ParserProcessor
public class PreTrainedParser extends NLPProcessor<ParserModel, ParserTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(PARSER_PARSER_TYPE.descriptor,
                                                                                                  PARSER_HEAD_RULES_FILE_PATH.descriptor,
                                                                                                  PARSER_PARSES_COUNT.descriptor,
                                                                                                  PARSER_ADVANCE_PERCENTAGE.descriptor,
                                                                                                  PARSER_BEAM_SIZE.descriptor))
                                                                                .collect(toList());

    public PreTrainedParser() {
        super(false);
    }

    @Override
    protected ParserTool createTool(Path modelPath) {
        return new ParserTool(modelPath, getLogger());
    }
}
