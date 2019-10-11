package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.langdetect.Language;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.*;
import static org.rdlopes.processors.opennlp.DetectLanguage.*;

public class DetectLanguageTest extends AbstractNlpProcessorTest {
    public DetectLanguageTest() {
        super(DetectLanguage.class, true);
    }

    @Test
    public void shouldDetectLanguageFromTimesheetQuestion() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/langdetect-183.bin").getFile());
        testRunner.enqueue("did I report my time correctly?");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE, "eng");
        flowFile.assertAttributeEquals(ATTRIBUTE_LANGDET_PREDICTED_LANGUAGE_CONFIDENCE, "0.024210225219043664");
        flowFile.assertAttributeExists(ATTRIBUTE_LANGDET_PROBABLE_LANGUAGE_LIST);
        List<Language> probableLanguageList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LANGDET_PROBABLE_LANGUAGE_LIST),
                                                                  new TypeToken<List<Language>>() {}.getType());
        flowFile.assertAttributeExists(ATTRIBUTE_LANGDET_SUPPORTED_LANGUAGE_LIST);
        List<Language> supportedLanguageList = new Gson().fromJson(flowFile.getAttribute(ATTRIBUTE_LANGDET_SUPPORTED_LANGUAGE_LIST),
                                                                   new TypeToken<List<String>>() {}.getType());
        assertThat(supportedLanguageList).hasSize(103);
        assertThat(probableLanguageList).hasSize(103)
                                        .startsWith(new Language("eng", 0.024210225219043664));
    }

    @Test
    public void shouldProduceNoResultWithoutInput() {
        testRunner.setProperty(PROPERTY_MODEL_FILE_PATH, getClass().getResource("/models/langdetect-183.bin").getFile());
        testRunner.enqueue();
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 0);
    }


}
