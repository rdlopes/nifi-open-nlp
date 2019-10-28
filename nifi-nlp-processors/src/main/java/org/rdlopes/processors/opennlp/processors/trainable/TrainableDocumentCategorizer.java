package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import org.rdlopes.processors.opennlp.wrappers.DocumentCategorizerWrapper;

public class TrainableDocumentCategorizer extends AbstractTrainableProcessor<DocumentCategorizer, DoccatModel> {

    public TrainableDocumentCategorizer() {
        super(new DocumentCategorizerWrapper(), true);
    }
}
