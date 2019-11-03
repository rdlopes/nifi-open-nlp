package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.parser.ParserModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.ParserTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

@EqualsAndHashCode(callSuper = true)
public class TrainableParser extends NLPProcessor<ParserModel, ParserTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(PARSER_PARSER_TYPE.descriptor,
                                                                                                  PARSER_HEAD_RULES_FILE_PATH.descriptor,
                                                                                                  PARSER_PARSES_COUNT.descriptor,
                                                                                                  PARSER_ADVANCE_PERCENTAGE.descriptor,
                                                                                                  PARSER_BEAM_SIZE.descriptor))
                                                                                .collect(toList());

    public TrainableParser() {
        super(true);
    }

    @Override
    protected ParserTool createTool(Path modelPath) {
        return new ParserTool(modelPath, getLogger());
    }
}
