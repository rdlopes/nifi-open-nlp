package org.rdlopes.processors.opennlp.tools;

import opennlp.tools.langdetect.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;

public class LanguageDetectorTool extends NLPTool<LanguageDetectorModel> {
    public LanguageDetectorTool(Path modelPath, ComponentLog logger) {
        super(LanguageDetectorModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, LanguageDetectorModel model, Map<String, String> evaluation)
            throws IOException {
        LanguageDetector languageDetector = new LanguageDetectorME(model);
        String contentString = IOUtils.toString(content, charset);

        Language predictedLanguage = languageDetector.predictLanguage(contentString);
        Language[] probableLanguageList = languageDetector.predictLanguages(contentString);
        String[] supportedLanguages = languageDetector.getSupportedLanguages();

        set(LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY, evaluation, probableLanguageList);
        set(LANGUAGE_DETECTOR_LANGUAGES_BEST_KEY, evaluation, predictedLanguage);
        set(LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY, evaluation, supportedLanguages);
    }

    @Override
    protected LanguageDetectorModel trainModel(ValidationContext validationContext,
                                               InputStreamFactory inputStreamFactory,
                                               TrainingParameters trainingParameters,
                                               String trainingLanguage) throws IOException {
        LanguageDetectorFactory factory = LanguageDetectorFactory.create(null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ObjectStream<LanguageSample> sampleStream = new LanguageDetectorSampleStream(lineStream)) {
            return LanguageDetectorME.train(sampleStream, trainingParameters, factory);
        }
    }
}
