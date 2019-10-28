package org.rdlopes.processors.opennlp.processors.trained;

import lombok.Getter;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.wrappers.TokenizerWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TOKENIZE_TOKENIZER_TYPE;

public class TrainedTokenizer extends AbstractPreTrainedProcessor<Tokenizer, TokenizerModel> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(TOKENIZE_TOKENIZER_TYPE.descriptor))
                                                                                .collect(toList());

    public TrainedTokenizer() {
        super(new TokenizerWrapper());
    }
}
