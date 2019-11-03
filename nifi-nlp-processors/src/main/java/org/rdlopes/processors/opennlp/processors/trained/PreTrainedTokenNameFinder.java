package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.namefind.TokenNameFinderModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.TokenNameFinderTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;

@EqualsAndHashCode(callSuper = true)
public class PreTrainedTokenNameFinder extends NLPProcessor<TokenNameFinderModel, TokenNameFinderTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(NAMEFIND_NAME_TYPE.descriptor))
                                                                                .collect(toList());

    public PreTrainedTokenNameFinder() {
        super(false);
    }

    @Override
    protected TokenNameFinderTool createTool(Path modelPath) {
        return new TokenNameFinderTool(modelPath, getLogger());
    }
}
