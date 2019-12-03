package org.rdlopes.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.doccat.DoccatModel;
import org.rdlopes.opennlp.processors.DocumentCategorizerProcessor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.tools.DocumentCategorizerTool;

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
