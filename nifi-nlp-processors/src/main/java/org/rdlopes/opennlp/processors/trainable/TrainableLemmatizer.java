package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.lemmatizer.LemmatizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.opennlp.common.NLPProperty;
import org.rdlopes.opennlp.processors.LemmatizerProcessor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.tools.LemmatizerTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@EqualsAndHashCode(callSuper = true)
@LemmatizerProcessor
public class TrainableLemmatizer extends NLPProcessor<LemmatizerModel, LemmatizerTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
            Stream.of(NLPProperty.LEMMATIZE_SEARCH_COUNT.descriptor,
                    NLPProperty.LEMMATIZE_TOPK_MINIMUM_SCORE.descriptor))
                                                                                .collect(toList());

    public TrainableLemmatizer() {
        super(true);
    }

    @Override
    protected LemmatizerTool createTool(Path modelPath) {
        return new LemmatizerTool(modelPath, getLogger());
    }
}
