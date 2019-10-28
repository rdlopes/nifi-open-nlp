package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceModel;
import org.rdlopes.processors.opennlp.wrappers.SentenceDetectorWrapper;

public class TrainableSentenceDetector extends AbstractTrainableProcessor<SentenceDetector, SentenceModel> {

    public TrainableSentenceDetector() {
        super(new SentenceDetectorWrapper(), true);
    }
}
