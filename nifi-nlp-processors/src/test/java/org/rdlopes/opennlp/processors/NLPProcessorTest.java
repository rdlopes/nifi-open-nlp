package org.rdlopes.opennlp.processors;

import org.rdlopes.opennlp.common.BaseProcessorTest;

public abstract class NLPProcessorTest<P extends NLPProcessor<?, ?>> extends BaseProcessorTest<P> {

    protected NLPProcessorTest(Class<P> processorClass) {
        super(processorClass);
    }

}
