package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.langdetect.LanguageDetectorModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.LanguageDetectorWrapper;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;

public class TrainedLanguageDetector extends AbstractPreTrainedProcessor<LanguageDetectorModel> {

    @Override
    protected NLPToolWrapper<LanguageDetectorModel> createWrapper(ProcessorInitializationContext context) {
        return new LanguageDetectorWrapper();
    }
}
