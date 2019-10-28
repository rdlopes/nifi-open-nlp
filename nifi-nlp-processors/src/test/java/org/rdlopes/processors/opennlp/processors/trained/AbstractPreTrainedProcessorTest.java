package org.rdlopes.processors.opennlp.processors.trained;

import org.junit.Test;
import org.rdlopes.processors.opennlp.processors.AbstractNLPProcessorTest;

import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;

public abstract class AbstractPreTrainedProcessorTest<P extends AbstractPreTrainedProcessor<?, ?>> extends AbstractNLPProcessorTest<P> {

    public AbstractPreTrainedProcessorTest(Class<P> processorClass) {
        super(processorClass);
    }

    protected void setModelFilePath(String filePath) {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource(filePath).getFile());
    }

    @Test
    public void shouldBeInvalidWithoutModelFile() {
        testRunner.assertNotValid();
    }
}
