package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.chunker.ChunkerModel;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;
import org.rdlopes.processors.opennlp.tools.ChunkerTool;

import java.nio.file.Path;

public class TrainableChunker extends NLPProcessor<ChunkerModel, ChunkerTool> {

    public TrainableChunker() {super(true);}

    @Override
    protected ChunkerTool createTool(Path modelPath) {
        return new ChunkerTool(modelPath, getLogger());
    }
}
