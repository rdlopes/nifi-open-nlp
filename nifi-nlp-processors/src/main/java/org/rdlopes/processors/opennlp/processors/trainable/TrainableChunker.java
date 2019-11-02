package org.rdlopes.processors.opennlp.processors.trainable;

import opennlp.tools.chunker.ChunkerModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.ChunkerWrapper;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;

public class TrainableChunker extends AbstractTrainableProcessor<ChunkerModel> {

    public TrainableChunker() {
        super(true);
    }

    @Override
    protected NLPToolWrapper<ChunkerModel> createWrapper(ProcessorInitializationContext context) {
        return new ChunkerWrapper();
    }
}
