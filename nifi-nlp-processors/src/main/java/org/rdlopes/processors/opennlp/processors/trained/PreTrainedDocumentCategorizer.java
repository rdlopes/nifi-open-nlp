package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.doccat.DoccatModel;
import org.rdlopes.processors.opennlp.processors.DocumentCategorizerProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.DocumentCategorizerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@DocumentCategorizerProcessor
public class PreTrainedDocumentCategorizer extends NLPProcessor<DoccatModel, DocumentCategorizerTool> {

    public PreTrainedDocumentCategorizer() {
        super(false);
    }

    @Override
    protected DocumentCategorizerTool createTool(Path modelPath) {
        return new DocumentCategorizerTool(modelPath, getLogger());
    }
}
