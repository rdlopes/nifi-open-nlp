package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.rdlopes.opennlp.processors.LanguageDetectorProcessor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.tools.LanguageDetectorTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@LanguageDetectorProcessor
public class TrainableLanguageDetector extends NLPProcessor<LanguageDetectorModel, LanguageDetectorTool> {

    public TrainableLanguageDetector() {
        super(true);
    }

    @Override
    protected LanguageDetectorTool createTool(Path modelPath) {
        return new LanguageDetectorTool(modelPath, getLogger());
    }
}
