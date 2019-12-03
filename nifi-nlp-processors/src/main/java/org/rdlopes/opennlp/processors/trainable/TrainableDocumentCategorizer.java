package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.doccat.DoccatModel;
import org.rdlopes.opennlp.processors.DocumentCategorizerProcessor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.tools.DocumentCategorizerTool;

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
