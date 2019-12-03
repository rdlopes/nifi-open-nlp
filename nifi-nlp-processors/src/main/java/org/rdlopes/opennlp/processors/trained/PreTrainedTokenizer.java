package org.rdlopes.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.tokenize.TokenizerModel;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.TokenizerProcessor;
import org.rdlopes.opennlp.tools.TokenizerTool;

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
