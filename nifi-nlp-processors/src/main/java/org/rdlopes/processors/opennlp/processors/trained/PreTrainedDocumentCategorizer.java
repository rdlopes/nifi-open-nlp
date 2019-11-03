package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.doccat.DoccatModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.DocumentCategorizerTool;

import java.nio.file.Path;

public class PreTrainedDocumentCategorizer extends NLPProcessor<DoccatModel, DocumentCategorizerTool> {

    public PreTrainedDocumentCategorizer() {
        super(false);
    }

    @Override
    protected DocumentCategorizerTool createTool(Path modelPath) {
        return new DocumentCategorizerTool(modelPath, getLogger());
    }
}
