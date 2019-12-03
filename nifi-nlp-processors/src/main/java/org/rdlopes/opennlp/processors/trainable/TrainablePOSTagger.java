package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.postag.POSModel;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.POSTaggerProcessor;
import org.rdlopes.opennlp.tools.POSTaggerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@POSTaggerProcessor
public class TrainablePOSTagger extends NLPProcessor<POSModel, POSTaggerTool> {

    public TrainablePOSTagger() {
        super(true);
    }

    @Override
    protected POSTaggerTool createTool(Path modelPath) {
        return new POSTaggerTool(modelPath, getLogger());
    }
}
