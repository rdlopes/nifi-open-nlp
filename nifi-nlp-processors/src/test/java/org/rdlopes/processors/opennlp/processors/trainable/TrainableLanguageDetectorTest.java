package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.langdetect.Language;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.List;

import static opennlp.tools.ml.naivebayes.NaiveBayesTrainer.NAIVE_BAYES_VALUE;
import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableLanguageDetectorTest extends TrainableProcessorTest<TrainableLanguageDetector> {

    public TrainableLanguageDetectorTest() {
        super(TrainableLanguageDetector.class);
    }

    @Test
    public void shouldDetectPortuguese() {
        setTrainingFilePath("/training/en-langdet.train");
        setTrainingParamAlgorithm(NAIVE_BAYES_VALUE);
        setTrainingParamCutoff(0);
        setTrainingParamIterations(100);

        testRunner.enqueue("estava em uma marcenaria na Rua Bruno");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(LANGDET_PREDICTED_LANGUAGE.key, "pob");
        flowFile.assertAttributeEquals(LANGDET_CONFIDENCE.key, "0.9999999829939245");
        flowFile.assertAttributeExists(LANGDET_PROBABLE_LANGUAGE_LIST.key);
        flowFile.assertAttributeExists(LANGDET_SUPPORTED_LANGUAGE_LIST.key);

        List<Language> probableLanguageList = LANGDET_PROBABLE_LANGUAGE_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Language>>() {});
        assertThat(probableLanguageList).containsExactly(
                new Language("pob", 0.9999999829939245),
                new Language("ita", 1.7006066448468345E-8),
                new Language("spa", 9.07907141044455E-15),
                new Language("fra", 2.315191024434162E-25));

        List<String> supportedLanguageList = LANGDET_SUPPORTED_LANGUAGE_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        assertThat(supportedLanguageList).containsExactly("pob", "spa", "fra", "ita");
    }

}
