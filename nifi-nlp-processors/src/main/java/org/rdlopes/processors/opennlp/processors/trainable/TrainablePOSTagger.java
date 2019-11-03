package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.postag.POSModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.POSTaggerTool;

import java.nio.file.Path;

public class TrainablePOSTagger extends NLPProcessor<POSModel, POSTaggerTool> {

    public TrainablePOSTagger() {
        super(true);
    }

    @Override
    protected POSTaggerTool createTool(Path modelPath) {
        return new POSTaggerTool(modelPath, getLogger());
    }
}
