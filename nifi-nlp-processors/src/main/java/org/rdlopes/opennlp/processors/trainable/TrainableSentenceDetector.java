package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.sentdetect.SentenceModel;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.SentenceDetectorProcessor;
import org.rdlopes.opennlp.tools.SentenceDetectorTool;

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
