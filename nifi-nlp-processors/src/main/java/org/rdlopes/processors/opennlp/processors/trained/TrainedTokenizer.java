package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;
import org.rdlopes.processors.opennlp.wrappers.TokenizerWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TOKENIZE_TOKENIZER_TYPE;

@EqualsAndHashCode(callSuper = true)
public class TrainedTokenizer extends AbstractPreTrainedProcessor<TokenizerModel> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(TOKENIZE_TOKENIZER_TYPE.descriptor))
                                                                                .collect(toList());

    @Override
    protected NLPToolWrapper<TokenizerModel> createWrapper(ProcessorInitializationContext context) {
        return new TokenizerWrapper();
    }
}
