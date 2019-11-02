package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.TokenizerTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TOKENIZE_TOKENIZER_TYPE;

@EqualsAndHashCode(callSuper = true)
public class TrainableTokenizer extends NLPProcessor<TokenizerModel, TokenizerTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(TOKENIZE_TOKENIZER_TYPE.descriptor))
                                                                                .collect(toList());

    public TrainableTokenizer() {
        super(true);
    }

    @Override
    protected TokenizerTool createTool(Path modelPath) {
        return new TokenizerTool(modelPath, getLogger());
    }
}
