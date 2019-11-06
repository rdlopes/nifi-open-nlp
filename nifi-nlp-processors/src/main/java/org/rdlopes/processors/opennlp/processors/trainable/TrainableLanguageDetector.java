package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.rdlopes.processors.opennlp.processors.LanguageDetectorProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.LanguageDetectorTool;

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
