package org.rdlopes.processors.opennlp.wrappers;

import opennlp.tools.langdetect.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;

public class LanguageDetectorWrapper extends NLPToolWrapper<LanguageDetectorModel> {

    public LanguageDetectorWrapper() {
        super(LanguageDetectorModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, LanguageDetectorModel model, String content, Map<String, String> attributes) {
        LanguageDetector languageDetector = new LanguageDetectorME(model);

        Language predictedLanguage = languageDetector.predictLanguage(content);
        LANGDET_PREDICTED_LANGUAGE.updateAttributesWithString(attributes, predictedLanguage.getLang());
        LANGDET_CONFIDENCE.updateAttributesWithString(attributes, predictedLanguage.getConfidence());

        Language[] probableLanguageList = languageDetector.predictLanguages(content);
        LANGDET_PROBABLE_LANGUAGE_LIST.updateAttributesWithJson(attributes, probableLanguageList);

        String[] supportedLanguages = languageDetector.getSupportedLanguages();
        LANGDET_SUPPORTED_LANGUAGE_LIST.updateAttributesWithJson(attributes, supportedLanguages);
    }

    @Override
    public LanguageDetectorModel trainModel(PropertyContext propertyContext,
                                            String trainingLanguage,
                                            Charset charset,
                                            TrainingParameters trainingParameters,
                                            InputStreamFactory inputStreamFactory) throws IOException {
        LanguageDetectorFactory factory = LanguageDetectorFactory.create(null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<LanguageSample> sampleStream = new LanguageDetectorSampleStream(lineStream)) {
            return LanguageDetectorME.train(sampleStream, trainingParameters, factory);
        }
    }
}
