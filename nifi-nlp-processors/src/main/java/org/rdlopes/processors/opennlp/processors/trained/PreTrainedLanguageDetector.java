package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.rdlopes.processors.opennlp.processors.LanguageDetectorProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.LanguageDetectorTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@LanguageDetectorProcessor
public class PreTrainedLanguageDetector extends NLPProcessor<LanguageDetectorModel, LanguageDetectorTool> {

    public PreTrainedLanguageDetector() {
        super(false);
    }

    @Override
    protected LanguageDetectorTool createTool(Path modelPath) {
        return new LanguageDetectorTool(modelPath, getLogger());
    }
}
