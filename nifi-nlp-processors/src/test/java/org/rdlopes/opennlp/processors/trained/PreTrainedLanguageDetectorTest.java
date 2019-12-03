package org.rdlopes.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.langdetect.Language;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.NLPAttribute;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PreTrainedLanguageDetectorTest extends PreTrainedProcessorTest<PreTrainedLanguageDetector> {
    public PreTrainedLanguageDetectorTest() {
        super(PreTrainedLanguageDetector.class, "/models/langdetect-183.bin");
    }

    @Test
    public void shouldDetectPortuguese() {
        testRunner.assertValid();

        testRunner.enqueue("estava em uma marcenaria na Rua Bruno");
        testRunner.run();
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(BaseProcessor.RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(NLPAttribute.LANGUAGE_DETECTOR_LANGUAGES_BEST_KEY, "{\"lang\":\"por\",\"confidence\":0.025462605870143394}");

        flowFile.assertAttributeExists(NLPAttribute.LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY);
        List<Language> languageList = NLPAttribute.get(NLPAttribute.LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<Language>>() {
        });
        flowFile.assertAttributeExists(NLPAttribute.LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY);
        List<String> supportedLanguageList = NLPAttribute.get(NLPAttribute.LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {
        });

        assertThat(languageList).hasSize(103)
                .startsWith(new Language("por", 0.025462605870143394));

        assertThat(supportedLanguageList).hasSize(103);
    }
}
