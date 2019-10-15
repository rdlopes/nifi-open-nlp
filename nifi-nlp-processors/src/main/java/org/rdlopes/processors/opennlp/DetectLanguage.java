package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import opennlp.tools.langdetect.*;
import opennlp.tools.util.*;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.rdlopes.processors.opennlp.DetectLanguage.*;

@NlpProcessor
@Tags({"apache", "nlp", "language", "detection"})
@CapabilityDescription("Detects the language used in the content of a flow file.")
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE, description = "Holds the language code predicted from flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE_CONFIDENCE, description = "Holds the confidence for the made prediction."),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_PROBABLE_LANGUAGE_LIST, description = "Holds the probable languages for the flow file content, as a JSON languages list."),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_SUPPORTED_LANGUAGE_LIST, description = "Holds the language list supported by NLP engine, as a JSON string list.")})
@EqualsAndHashCode(callSuper = true)
public class DetectLanguage extends AbstractNlpProcessor<LanguageDetectorModel> {

    static final String ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE = "nlp.langdet.predicted.language";

    static final String ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE_CONFIDENCE = "nlp.langdet.predicted.language.confidence";

    static final String ATTRIBUTE_LANGDET_PROBABLE_LANGUAGE_LIST = "nlp.langdet.probable.language.list";

    static final String ATTRIBUTE_LANGDET_SUPPORTED_LANGUAGE_LIST = "nlp.langdet.supported.language.list";

    public DetectLanguage() {super(LanguageDetectorModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, String content, Map<String, String> attributes) {
        // LanguageDetectorME
        Map<String, String> evaluation = new HashMap<>();
        LanguageDetector languageDetector = new LanguageDetectorME(getModel());
        Language predictedLanguage = languageDetector.predictLanguage(content);
        Language[] probableLanguageList = languageDetector.predictLanguages(content);
        String[] supportedLanguages = languageDetector.getSupportedLanguages();

        evaluation.put(ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE, predictedLanguage.getLang());
        evaluation.put(ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE_CONFIDENCE, String.valueOf(predictedLanguage.getConfidence()));
        evaluation.put(ATTRIBUTE_LANGDET_PROBABLE_LANGUAGE_LIST, new Gson().toJson(probableLanguageList));
        evaluation.put(ATTRIBUTE_LANGDET_SUPPORTED_LANGUAGE_LIST, new Gson().toJson(supportedLanguages));

        return evaluation;
    }

    private LanguageDetectorModel trainModelFrom(TrainingParameters trainingParameters, Charset charset, InputStreamFactory inputStreamFactory)
            throws IOException {
        LanguageDetectorFactory factory = LanguageDetectorFactory.create(null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<LanguageSample> sampleStream = new LanguageDetectorSampleStream(lineStream)) {
            return LanguageDetectorME.train(sampleStream, trainingParameters, factory);
        }
    }

    @Override
    protected LanguageDetectorModel trainModelFromData(ValidationContext validationContext, TrainingParameters trainingParameters, Charset charset, String trainingData) throws IOException {
        return trainModelFrom(trainingParameters, charset, () -> toInputStream(trainingData, charset));
    }

    @Override
    protected LanguageDetectorModel trainModelFromFile(ValidationContext validationContext, TrainingParameters trainingParameters, Charset charset, File dataFile) throws IOException {
        return trainModelFrom(trainingParameters, charset, new MarkableFileInputStreamFactory(dataFile));
    }
}
