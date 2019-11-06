package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.doccat.DoccatModel;
import org.rdlopes.processors.opennlp.processors.DocumentCategorizerProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.DocumentCategorizerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@DocumentCategorizerProcessor
public class TrainableDocumentCategorizer extends NLPProcessor<DoccatModel, DocumentCategorizerTool> {

    public TrainableDocumentCategorizer() {
        super(true);
    }

    @Override
    protected DocumentCategorizerTool createTool(Path modelPath) {
        return new DocumentCategorizerTool(modelPath, getLogger());
    }
}
