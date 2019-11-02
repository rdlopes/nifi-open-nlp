package org.rdlopes.processors.opennlp.processors.trained;

import opennlp.tools.chunker.ChunkerModel;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.rdlopes.processors.opennlp.wrappers.ChunkerWrapper;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;

public class TrainedChunker extends AbstractPreTrainedProcessor<ChunkerModel> {


    @Override
    protected NLPToolWrapper<ChunkerModel> createWrapper(ProcessorInitializationContext context) {
        return new ChunkerWrapper();
    }
}
