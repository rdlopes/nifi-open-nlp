package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.TokenizerTool;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TOKENIZE_TOKENIZER_TYPE;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;

@EqualsAndHashCode(callSuper = true)
public class PreConfiguredTokenizer extends NLPProcessor<TokenizerModel, TokenizerTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(TOKENIZE_TOKENIZER_TYPE.descriptor))
                                                                                .filter(d -> d != TRAINED_MODEL_FILE_PATH.descriptor)
                                                                                .collect(toList());

    public PreConfiguredTokenizer() {
        super(false);
    }

    @Override
    protected TokenizerTool createTool(Path modelPath) {
        return new TokenizerTool(modelPath, getLogger());
    }

    @Override
    protected void validatePreTrainedModel(ValidationContext validationContext, Collection<ValidationResult> results) {
        // no op - not used in this processor
    }
}
