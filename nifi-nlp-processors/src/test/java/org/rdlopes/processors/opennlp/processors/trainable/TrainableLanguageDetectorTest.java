package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.langdetect.Language;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

import static opennlp.tools.ml.naivebayes.NaiveBayesTrainer.NAIVE_BAYES_VALUE;
import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableLanguageDetectorTest extends TrainableProcessorTest<TrainableLanguageDetector> {

    public TrainableLanguageDetectorTest() {
        super(TrainableLanguageDetector.class);
    }

    @Test
    public void shouldDetectPortuguese() throws URISyntaxException {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath("/training/en-langdet.train").toString());
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ALGORITHM.descriptor, NAIVE_BAYES_VALUE);
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(0));
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(100));
        testRunner.assertValid();

        testRunner.enqueue("estava em uma marcenaria na Rua Bruno");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(LANGUAGE_DETECTOR_LANGUAGES_BEST_KEY, "{\"lang\":\"pob\",\"confidence\":0.9999999829939245}");

        flowFile.assertAttributeExists(LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY);
        List<Language> languageList = get(LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<Language>>() {});
        flowFile.assertAttributeExists(LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY);
        List<String> supportedLanguageList = get(LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});

        assertThat(languageList).containsExactly(
                new Language("pob", 0.9999999829939245),
                new Language("ita", 1.7006066448468345E-8),
                new Language("spa", 9.07907141044455E-15),
                new Language("fra", 2.315191024434162E-25));

        assertThat(supportedLanguageList).containsExactly("pob", "spa", "fra", "ita");
    }

}
