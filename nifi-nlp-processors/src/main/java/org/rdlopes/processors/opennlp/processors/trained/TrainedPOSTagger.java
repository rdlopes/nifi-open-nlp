package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.postag.POSModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;
import org.rdlopes.processors.opennlp.wrappers.POSTaggerWrapper;

public class TrainedPOSTagger extends AbstractPreTrainedProcessor<POSModel> {

    @Override
    protected NLPToolWrapper<POSModel> createWrapper(ProcessorInitializationContext context) {
        return new POSTaggerWrapper();
    }
}
