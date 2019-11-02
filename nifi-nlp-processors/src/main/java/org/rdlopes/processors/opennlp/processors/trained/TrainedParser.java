package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.parser.ParserModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;
import org.rdlopes.processors.opennlp.wrappers.ParserWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

@EqualsAndHashCode(callSuper = true)
public class TrainedParser extends AbstractPreTrainedProcessor<ParserModel> {


    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(PARSER_PARSER_TYPE.descriptor,
                                                                                                  PARSER_HEAD_RULES_FILE_PATH.descriptor,
                                                                                                  PARSER_PARSES_COUNT.descriptor,
                                                                                                  PARSER_ADVANCE_PERCENTAGE.descriptor,
                                                                                                  PARSER_BEAM_SIZE.descriptor))
                                                                                .collect(toList());

    @Override
    protected NLPToolWrapper<ParserModel> createWrapper(ProcessorInitializationContext context) {
        return new ParserWrapper();
    }
}
