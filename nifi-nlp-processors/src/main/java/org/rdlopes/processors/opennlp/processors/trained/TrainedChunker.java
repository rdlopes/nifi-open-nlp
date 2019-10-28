package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerModel;
import org.rdlopes.processors.opennlp.wrappers.ChunkerWrapper;

public class TrainedChunker extends AbstractPreTrainedProcessor<Chunker, ChunkerModel> {

    public TrainedChunker() {
        super(new ChunkerWrapper());
    }
}
