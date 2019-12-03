package org.rdlopes.opennlp.processors.trainable;

import org.rdlopes.opennlp.common.NLPProperty;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.NLPProcessorTest;

public abstract class TrainableProcessorTest<P extends NLPProcessor<?, ?>> extends NLPProcessorTest<P> {
    private final String trainingFilePath;

    protected TrainableProcessorTest(Class<P> processorClass, String trainingFilePath) {
        super(processorClass);
        this.trainingFilePath = trainingFilePath;
    }

    @Override
    public void init() throws Exception {
        super.init();
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath(trainingFilePath).toString());
    }

}
