package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.tokenize.TokenizerModel;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.TokenizerProcessor;
import org.rdlopes.opennlp.tools.TokenizerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@TokenizerProcessor
public class TrainableTokenizer extends NLPProcessor<TokenizerModel, TokenizerTool> {

    public TrainableTokenizer() {
        super(true);
    }

    @Override
    protected TokenizerTool createTool(Path modelPath) {
        return new TokenizerTool(modelPath, getLogger());
    }
}
