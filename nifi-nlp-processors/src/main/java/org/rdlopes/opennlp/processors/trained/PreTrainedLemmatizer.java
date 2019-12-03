package org.rdlopes.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.lemmatizer.LemmatizerModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.rdlopes.opennlp.processors.LemmatizerProcessor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.tools.LemmatizerTool;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.opennlp.common.NLPProperty.LEMMATIZE_SEARCH_COUNT;
import static org.rdlopes.opennlp.common.NLPProperty.LEMMATIZE_TOPK_MINIMUM_SCORE;

@EqualsAndHashCode(callSuper = true)
@LemmatizerProcessor
public class PreTrainedLemmatizer extends NLPProcessor<LemmatizerModel, LemmatizerTool> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(LEMMATIZE_SEARCH_COUNT.descriptor,
                                                                                                  LEMMATIZE_TOPK_MINIMUM_SCORE.descriptor))
                                                                                .collect(toList());

    public PreTrainedLemmatizer() {
        super(false);
    }

    @Override
    protected LemmatizerTool createTool(Path modelPath) {
        return new LemmatizerTool(modelPath, getLogger());
    }
}
