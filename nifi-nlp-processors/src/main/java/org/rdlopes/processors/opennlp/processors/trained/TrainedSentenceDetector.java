package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.sentdetect.SentenceModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;
import org.rdlopes.processors.opennlp.wrappers.SentenceDetectorWrapper;

public class TrainedSentenceDetector extends AbstractPreTrainedProcessor<SentenceModel> {

    @Override
    protected NLPToolWrapper<SentenceModel> createWrapper(ProcessorInitializationContext context) {
        return new SentenceDetectorWrapper();
    }
}
