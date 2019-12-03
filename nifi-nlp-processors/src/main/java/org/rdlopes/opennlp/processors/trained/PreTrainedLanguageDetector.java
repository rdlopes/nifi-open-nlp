package org.rdlopes.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.rdlopes.opennlp.processors.LanguageDetectorProcessor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.tools.LanguageDetectorTool;

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
