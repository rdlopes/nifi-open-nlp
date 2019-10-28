package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.Getter;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.lemmatizer.LemmatizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.wrappers.LemmatizerWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_SEARCH_COUNT;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_TOPK_MINIMUM_SCORE;

public class TrainableLemmatizer extends AbstractTrainableProcessor<Lemmatizer, LemmatizerModel> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(LEMMATIZE_SEARCH_COUNT.descriptor,
                                                                                                  LEMMATIZE_TOPK_MINIMUM_SCORE.descriptor))
                                                                                .collect(toList());

    public TrainableLemmatizer() {
        super(new LemmatizerWrapper(), true);
    }
}
