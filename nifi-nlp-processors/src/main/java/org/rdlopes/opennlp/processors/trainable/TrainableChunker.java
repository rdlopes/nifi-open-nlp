package org.rdlopes.opennlp.processors.trainable;

import lombok.EqualsAndHashCode;
import opennlp.tools.chunker.ChunkerModel;
import org.rdlopes.opennlp.processors.ChunkerProcessor;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.tools.ChunkerTool;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@ChunkerProcessor
public class TrainableChunker extends NLPProcessor<ChunkerModel, ChunkerTool> {

    public TrainableChunker() {super(true);}

    @Override
    protected ChunkerTool createTool(Path modelPath) {
        return new ChunkerTool(modelPath, getLogger());
    }
}
