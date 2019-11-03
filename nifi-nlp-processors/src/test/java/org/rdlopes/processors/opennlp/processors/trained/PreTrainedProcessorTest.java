package org.rdlopes.processors.opennlp.processors.trained;

import org.rdlopes.processors.opennlp.common.TokenizerType;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessorTest;

import static org.rdlopes.processors.opennlp.common.NLPProperty.TOKENIZE_TOKENIZER_TYPE;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;

public abstract class PreTrainedProcessorTest<P extends NLPProcessor<?, ?>> extends NLPProcessorTest<P> {
    PreTrainedProcessorTest(Class<P> processorClass) {
        super(processorClass);
    }

    protected void setModelFilePath(String filePath) {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource(filePath).getFile());
    }

    protected void setTokenizerType(TokenizerType type) {
        testRunner.setProperty(TOKENIZE_TOKENIZER_TYPE.descriptor, type.name());
    }
}
