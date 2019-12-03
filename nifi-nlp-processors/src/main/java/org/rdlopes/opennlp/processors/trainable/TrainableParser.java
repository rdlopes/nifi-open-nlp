package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.parser.ParserModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.opennlp.common.NLPProperty;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.ParserProcessor;
import org.rdlopes.opennlp.tools.ParserTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@EqualsAndHashCode(callSuper = true)
@ParserProcessor
public class TrainableParser extends NLPProcessor<ParserModel, ParserTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
            Stream.of(NLPProperty.PARSER_PARSER_TYPE.descriptor,
                    NLPProperty.PARSER_HEAD_RULES_FILE_PATH.descriptor,
                    NLPProperty.PARSER_PARSES_COUNT.descriptor,
                    NLPProperty.PARSER_ADVANCE_PERCENTAGE.descriptor,
                    NLPProperty.PARSER_BEAM_SIZE.descriptor))
                                                                                .collect(toList());

    public TrainableParser() {
        super(true);
    }

    @Override
    protected ParserTool createTool(Path modelPath) {
        return new ParserTool(modelPath, getLogger());
    }
}
