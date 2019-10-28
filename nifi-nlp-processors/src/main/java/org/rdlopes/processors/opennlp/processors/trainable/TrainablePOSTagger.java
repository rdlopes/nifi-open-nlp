package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import org.rdlopes.processors.opennlp.wrappers.POSTaggerWrapper;

public class TrainablePOSTagger extends AbstractTrainableProcessor<POSTagger, POSModel> {

    public TrainablePOSTagger() {
        super(new POSTaggerWrapper(), true);
    }
}
