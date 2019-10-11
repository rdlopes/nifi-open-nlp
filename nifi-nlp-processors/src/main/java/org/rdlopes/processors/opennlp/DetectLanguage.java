package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import opennlp.tools.langdetect.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR_DESCRIPTION;
import static org.rdlopes.processors.opennlp.DetectLanguage.*;

@NlpProcessor
@Tags({"apache", "nlp", "language", "detection"})
@CapabilityDescription("Detects the language used in the content of a flow file.")
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR,
                                    description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE,
                                    description = "Holds the language code predicted from flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE_CONFIDENCE,
                                    description = "Holds the confidence for the made prediction."),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_PROBABLE_LANGUAGE_LIST,
                                    description = "Holds the probable languages for the flow file content, as a JSON languages list."),
                   @WritesAttribute(attribute = ATTRIBUTE_LANGDET_SUPPORTED_LANGUAGE_LIST,
                                    description = "Holds the language list supported by NLP engine, as a JSON string list.")})
@EqualsAndHashCode(callSuper = true)
public class DetectLanguage extends AbstractNlpProcessor<LanguageDetectorModel> {

    public static final String ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE = "nlp.langdet.predicted.language";

    public static final String ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE_CONFIDENCE = "nlp.langdet.predicted.language.confidence";

    public static final String ATTRIBUTE_LANGDET_PROBABLE_LANGUAGE_LIST = "nlp.langdet.probable.language.list";

    public static final String ATTRIBUTE_LANGDET_SUPPORTED_LANGUAGE_LIST = "nlp.langdet.supported.language.list";

    public DetectLanguage() {super(LanguageDetectorModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, ProcessSession session, String content, Map<String, String> attributes) {
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

    @Override
    protected LanguageDetectorModel doTrain(ValidationContext context, TrainingParameters parameters, Charset charset, ObjectStream<String> stream) throws IOException {
        LanguageDetectorFactory factory = LanguageDetectorFactory.create(null);
        try (ObjectStream<LanguageSample> sampleStream = new LanguageDetectorSampleStream(stream)) {
            return LanguageDetectorME.train(sampleStream, parameters, factory);
        }
    }
}
