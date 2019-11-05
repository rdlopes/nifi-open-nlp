package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import opennlp.tools.chunker.ChunkerModel;
import org.rdlopes.processors.opennlp.processors.ChunkerProcessor;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.ChunkerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@ChunkerProcessor
public class PreTrainedChunker extends NLPProcessor<ChunkerModel, ChunkerTool> {

    public PreTrainedChunker() {
        super(false);
    }

    @Override
    protected ChunkerTool createTool(Path modelPath) {
        return new ChunkerTool(modelPath, getLogger());
    }
}
