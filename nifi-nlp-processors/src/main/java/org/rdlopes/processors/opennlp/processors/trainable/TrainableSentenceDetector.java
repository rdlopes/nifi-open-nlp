package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.sentdetect.SentenceModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.processors.SentenceDetectorProcessor;
import org.rdlopes.processors.opennlp.tools.SentenceDetectorTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@SentenceDetectorProcessor
public class TrainableSentenceDetector extends NLPProcessor<SentenceModel, SentenceDetectorTool> {

    public TrainableSentenceDetector() {
        super(true);
    }

    @Override
    protected SentenceDetectorTool createTool(Path modelPath) {
        return new SentenceDetectorTool(modelPath, getLogger());
    }
}
