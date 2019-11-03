package org.rdlopes.processors.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.tokenize.TokenizerModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.TokenizerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
public class TrainableTokenizer extends NLPProcessor<TokenizerModel, TokenizerTool> {

    public TrainableTokenizer() {
        super(true);
    }

    @Override
    protected TokenizerTool createTool(Path modelPath) {
        return new TokenizerTool(modelPath, getLogger());
    }
}
