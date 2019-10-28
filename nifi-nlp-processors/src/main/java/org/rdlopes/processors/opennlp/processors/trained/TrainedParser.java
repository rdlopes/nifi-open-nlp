package org.rdlopes.processors.opennlp.processors.trained;

import lombok.Getter;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.wrappers.ParserWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

public class TrainedParser extends AbstractPreTrainedProcessor<Parser, ParserModel> {


    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(PARSER_PARSER_TYPE.descriptor,
                                                                                                  PARSER_HEAD_RULES_FILE_PATH.descriptor,
                                                                                                  PARSER_PARSES_COUNT.descriptor,
                                                                                                  PARSER_ADVANCE_PERCENTAGE.descriptor,
                                                                                                  PARSER_BEAM_SIZE.descriptor))
                                                                                .collect(toList());

    public TrainedParser() {
        super(new ParserWrapper());
    }
}
