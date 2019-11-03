package org.rdlopes.processors.opennlp.processors.trained;

import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessorTest;

public abstract class PreTrainedProcessorTest<P extends NLPProcessor<?, ?>> extends NLPProcessorTest<P> {
    PreTrainedProcessorTest(Class<P> processorClass) {
        super(processorClass);
    }
}
