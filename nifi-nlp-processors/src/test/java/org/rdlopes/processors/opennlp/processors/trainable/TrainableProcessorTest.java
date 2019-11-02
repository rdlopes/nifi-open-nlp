package org.rdlopes.processors.opennlp.processors.trainable;

import org.junit.Test;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessorTest;

import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

public abstract class TrainableProcessorTest<P extends NLPProcessor<?, ?>> extends NLPProcessorTest<P> {
    protected TrainableProcessorTest(Class<P> processorClass) {
        super(processorClass);
    }

    void setHeadRulesFilePath(String headRulesFilePath) {
        testRunner.setProperty(PARSER_HEAD_RULES_FILE_PATH.descriptor, getClass().getResource(headRulesFilePath).getFile());
    }

    void setTrainingFilePath(String trainingFilePath) {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getClass().getResource(trainingFilePath).getFile());
    }

    void setTrainingParamAlgorithm(String algorithm) {
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ALGORITHM.descriptor, algorithm);
    }

    void setTrainingParamCutoff(int cutoff) {
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(cutoff));
    }

    void setTrainingParamIterations(int iterations) {
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(iterations));
    }

    @Test
    public void shouldBeInvalidWithoutTrainingParameters() {
        testRunner.assertNotValid();
    }

}
