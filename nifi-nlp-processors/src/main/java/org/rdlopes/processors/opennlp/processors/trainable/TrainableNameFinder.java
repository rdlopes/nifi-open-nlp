package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.namefind.TokenNameFinderModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;
import org.rdlopes.processors.opennlp.wrappers.NameFinderWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;

@EqualsAndHashCode(callSuper = true)
public class TrainableNameFinder extends AbstractTrainableProcessor<TokenNameFinderModel> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(NAMEFIND_NAME_TYPE.descriptor))
                                                                                .collect(toList());

    public TrainableNameFinder() {
        super(true);
    }

    @Override
    protected NLPToolWrapper<TokenNameFinderModel> createWrapper(ProcessorInitializationContext context) {
        return new NameFinderWrapper();
    }
}
