package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.rdlopes.processors.opennlp.wrappers.LanguageDetectorWrapper;

public class TrainedLanguageDetector extends AbstractPreTrainedProcessor<LanguageDetector, LanguageDetectorModel> {

    public TrainedLanguageDetector() {
        super(new LanguageDetectorWrapper());
    }
}
