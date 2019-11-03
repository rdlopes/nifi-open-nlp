package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.lemmatizer.LemmatizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.LemmatizerTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_SEARCH_COUNT;
import static org.rdlopes.processors.opennlp.common.NLPProperty.LEMMATIZE_TOPK_MINIMUM_SCORE;

@EqualsAndHashCode(callSuper = true)
public class TrainableLemmatizer extends NLPProcessor<LemmatizerModel, LemmatizerTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(LEMMATIZE_SEARCH_COUNT.descriptor,
                                                                                                  LEMMATIZE_TOPK_MINIMUM_SCORE.descriptor))
                                                                                .collect(toList());

    public TrainableLemmatizer() {
        super(true);
    }

    @Override
    protected LemmatizerTool createTool(Path modelPath) {
        return new LemmatizerTool(modelPath, getLogger());
    }
}
