package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.postag.POSModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;
import org.rdlopes.processors.opennlp.wrappers.POSTaggerWrapper;

public class TrainablePOSTagger extends AbstractTrainableProcessor<POSModel> {

    public TrainablePOSTagger() {
        super(true);
    }

    @Override
    protected NLPToolWrapper<POSModel> createWrapper(ProcessorInitializationContext context) {
        return new POSTaggerWrapper();
    }
}
