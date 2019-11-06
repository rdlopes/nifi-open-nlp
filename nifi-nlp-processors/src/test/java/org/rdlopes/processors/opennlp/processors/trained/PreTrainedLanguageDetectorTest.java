package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.langdetect.Language;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedLanguageDetectorTest extends PreTrainedProcessorTest<PreTrainedLanguageDetector> {
    public PreTrainedLanguageDetectorTest() {
        super(PreTrainedLanguageDetector.class);
    }

    @Test
    public void shouldDetectPortuguese() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/langdetect-183.bin").getFile());
        testRunner.enqueue("estava em uma marcenaria na Rua Bruno");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(LANGUAGE_DETECTOR_LANGUAGES_BEST_KEY, "{\"lang\":\"por\",\"confidence\":0.025462605870143394}");

        flowFile.assertAttributeExists(LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY);
        List<Language> languageList = get(LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<Language>>() {});
        flowFile.assertAttributeExists(LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY);
        List<String> supportedLanguageList = get(LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});

        assertThat(languageList).hasSize(103)
                                .startsWith(new Language("por", 0.025462605870143394));

        assertThat(supportedLanguageList).hasSize(103);
    }
}
