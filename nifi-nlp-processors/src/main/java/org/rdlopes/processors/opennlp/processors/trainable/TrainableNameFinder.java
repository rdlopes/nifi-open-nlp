package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.Getter;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.wrappers.NameFinderWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;

public class TrainableNameFinder extends AbstractTrainableProcessor<TokenNameFinder, TokenNameFinderModel> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(NAMEFIND_NAME_TYPE.descriptor))
                                                                                .collect(toList());

    public TrainableNameFinder() {
        super(new NameFinderWrapper(), true);
    }
}
