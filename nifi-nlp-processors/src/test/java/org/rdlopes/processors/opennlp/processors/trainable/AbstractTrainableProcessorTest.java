package org.rdlopes.processors.opennlp.processors.trainable;

import org.junit.Test;
import org.rdlopes.processors.opennlp.processors.AbstractNLPProcessorTest;

import static org.rdlopes.processors.opennlp.common.NLPProperty.*;

public abstract class AbstractTrainableProcessorTest<P extends AbstractTrainableProcessor<?>> extends AbstractNLPProcessorTest<P> {

    protected static String[] SAMPLE_TAGS_AGREEMENT = {"NNP", "VBD", "DT", "NN", "VBZ", "IN", "PRP", "TO", "VB",
                                                       "CD", "JJ", "JJ", "NNS", "IN", "DT", "NNS", "."};

    protected static String[] SAMPLE_TOKENS_AGREEMENT = {"Rockwell", "said", "the", "agreement", "calls", "for",
                                                         "it", "to", "supply", "200", "additional", "so-called", "shipsets",
                                                         "for", "the", "planes", "."};

    public AbstractTrainableProcessorTest(Class<P> processorClass) {
        super(processorClass);
    }

    protected void setHeadRulesFilePath(String headRulesFilePath) {
        testRunner.setProperty(PARSER_HEAD_RULES_FILE_PATH.descriptor, getClass().getResource(headRulesFilePath).getFile());
    }

    protected void setParsesCount(int count) {
        testRunner.setProperty(PARSER_PARSES_COUNT.descriptor, String.valueOf(count));
    }

    protected void setTrainingData(String trainingData) {
        testRunner.setProperty(TRAINABLE_TRAINING_DATA.descriptor, trainingData);
    }

    protected void setTrainingFilePath(String trainingFilePath) {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getClass().getResource(trainingFilePath).getFile());
    }

    protected void setTrainingParamAlgorithm(String algorithm) {
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ALGORITHM.descriptor, algorithm);
    }

    protected void setTrainingParamCutoff(int cutoff) {
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(cutoff));
    }

    protected void setTrainingParamIterations(int iterations) {
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(iterations));
    }

    @Test
    public void shouldBeInvalidWithoutTrainingParameters() {
        testRunner.assertNotValid();
    }

}
