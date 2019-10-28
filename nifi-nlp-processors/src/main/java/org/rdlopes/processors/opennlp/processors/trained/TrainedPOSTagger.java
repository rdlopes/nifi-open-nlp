package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import org.rdlopes.processors.opennlp.wrappers.POSTaggerWrapper;

public class TrainedPOSTagger extends AbstractPreTrainedProcessor<POSTagger, POSModel> {

    public TrainedPOSTagger() {
        super(new POSTaggerWrapper());
    }
}
