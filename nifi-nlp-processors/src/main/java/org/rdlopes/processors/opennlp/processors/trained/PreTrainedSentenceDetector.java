package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.sentdetect.SentenceModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.processors.SentenceDetectorProcessor;
import org.rdlopes.processors.opennlp.tools.SentenceDetectorTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@SentenceDetectorProcessor
public class PreTrainedSentenceDetector extends NLPProcessor<SentenceModel, SentenceDetectorTool> {

    public PreTrainedSentenceDetector() {
        super(false);
    }

    @Override
    protected SentenceDetectorTool createTool(Path modelPath) {
        return new SentenceDetectorTool(modelPath, getLogger());
    }
}
