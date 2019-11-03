package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.sentdetect.SentenceModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.SentenceDetectorTool;

import java.nio.file.Path;

public class PreTrainedSentenceDetector extends NLPProcessor<SentenceModel, SentenceDetectorTool> {

    public PreTrainedSentenceDetector() {
        super(false);
    }

    @Override
    protected SentenceDetectorTool createTool(Path modelPath) {
        return new SentenceDetectorTool(modelPath, getLogger());
    }
}
