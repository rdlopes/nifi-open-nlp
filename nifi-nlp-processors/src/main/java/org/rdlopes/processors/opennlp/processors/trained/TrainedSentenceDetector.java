package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceModel;
import org.rdlopes.processors.opennlp.wrappers.SentenceDetectorWrapper;

public class TrainedSentenceDetector extends AbstractPreTrainedProcessor<SentenceDetector, SentenceModel> {

    public TrainedSentenceDetector() {
        super(new SentenceDetectorWrapper());
    }
}
