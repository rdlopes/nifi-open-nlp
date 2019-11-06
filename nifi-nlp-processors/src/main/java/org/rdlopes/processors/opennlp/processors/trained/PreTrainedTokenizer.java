package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.tokenize.TokenizerModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.processors.TokenizerProcessor;
import org.rdlopes.processors.opennlp.tools.TokenizerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@TokenizerProcessor
public class PreTrainedTokenizer extends NLPProcessor<TokenizerModel, TokenizerTool> {

    public PreTrainedTokenizer() {
        super(false);
    }

    @Override
    protected TokenizerTool createTool(Path modelPath) {
        return new TokenizerTool(modelPath, getLogger());
    }
}
