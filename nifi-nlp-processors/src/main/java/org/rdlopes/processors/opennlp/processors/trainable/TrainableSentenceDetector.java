package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.sentdetect.SentenceModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;
import org.rdlopes.processors.opennlp.wrappers.SentenceDetectorWrapper;

public class TrainableSentenceDetector extends AbstractTrainableProcessor<SentenceModel> {

    public TrainableSentenceDetector() {
        super(true);
    }

    @Override
    protected NLPToolWrapper<SentenceModel> createWrapper(ProcessorInitializationContext context) {
        return new SentenceDetectorWrapper();
    }
}
