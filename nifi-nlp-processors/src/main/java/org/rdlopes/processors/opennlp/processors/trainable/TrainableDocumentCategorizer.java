package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.doccat.DoccatModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.DocumentCategorizerWrapper;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;

public class TrainableDocumentCategorizer extends AbstractTrainableProcessor<DoccatModel> {

    public TrainableDocumentCategorizer() {
        super(true);
    }

    @Override
    protected NLPToolWrapper<DoccatModel> createWrapper(ProcessorInitializationContext context) {
        return new DocumentCategorizerWrapper();
    }
}
