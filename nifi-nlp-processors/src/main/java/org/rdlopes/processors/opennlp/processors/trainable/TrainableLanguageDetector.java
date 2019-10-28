package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.rdlopes.processors.opennlp.wrappers.LanguageDetectorWrapper;

public class TrainableLanguageDetector extends AbstractTrainableProcessor<LanguageDetector, LanguageDetectorModel> {

    public TrainableLanguageDetector() {
        super(new LanguageDetectorWrapper(), true);
    }
}
