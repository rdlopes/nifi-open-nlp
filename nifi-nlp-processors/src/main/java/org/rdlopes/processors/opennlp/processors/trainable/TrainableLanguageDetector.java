package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.langdetect.LanguageDetectorModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.LanguageDetectorWrapper;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;

public class TrainableLanguageDetector extends AbstractTrainableProcessor<LanguageDetectorModel> {

    public TrainableLanguageDetector() {
        super(true);
    }

    @Override
    protected NLPToolWrapper<LanguageDetectorModel> createWrapper(ProcessorInitializationContext context) {
        return new LanguageDetectorWrapper();
    }
}
