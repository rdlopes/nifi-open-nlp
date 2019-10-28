package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerModel;
import org.rdlopes.processors.opennlp.wrappers.ChunkerWrapper;

public class TrainableChunker extends AbstractTrainableProcessor<Chunker, ChunkerModel> {

    public TrainableChunker() {
        super(new ChunkerWrapper(), true);
    }
}
