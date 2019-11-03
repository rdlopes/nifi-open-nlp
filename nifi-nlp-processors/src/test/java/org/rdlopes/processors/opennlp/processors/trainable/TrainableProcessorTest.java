package org.rdlopes.processors.opennlp.processors.trainable;

import org.junit.Test;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessorTest;

public abstract class TrainableProcessorTest<P extends NLPProcessor<?, ?>> extends NLPProcessorTest<P> {
    protected TrainableProcessorTest(Class<P> processorClass) {
        super(processorClass);
    }

    @Test
    public void shouldBeInvalidWithoutTrainingParameters() {
        testRunner.assertNotValid();
    }

}
