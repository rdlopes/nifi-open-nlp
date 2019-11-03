package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.postag.POSModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.POSTaggerTool;

import java.nio.file.Path;

public class PreTrainedPOSTagger extends NLPProcessor<POSModel, POSTaggerTool> {

    public PreTrainedPOSTagger() {
        super(false);
    }

    @Override
    protected POSTaggerTool createTool(Path modelPath) {
        return new POSTaggerTool(modelPath, getLogger());
    }
}
