package org.rdlopes.processors.opennlp.wrappers;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.chunker.*;
import opennlp.tools.util.*;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;

public class ChunkerWrapper extends NLPToolWrapper<Chunker, ChunkerModel> {

    public ChunkerWrapper() {
        super(ChunkerModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, ChunkerModel model, String content, Map<String, String> attributes) {
        String[] tagsList = TAGPOS_TAG_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});
        String[] tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});

        ChunkerME chunker = new ChunkerME(model);
        String[] chunkList = chunker.chunk(tokensList, tagsList);
        Span[] spanList = chunker.chunkAsSpans(tokensList, tagsList);

        CHUNK_CHUNK_LIST.updateAttributesWithJson(attributes, chunkList);
        CHUNK_SPAN_LIST.updateAttributesWithJson(attributes, spanList);
    }

    @Override
    public ChunkerModel trainModel(PropertyContext propertyContext,
                                   String trainingLanguage,
                                   Charset charset,
                                   TrainingParameters trainingParameters,
                                   InputStreamFactory inputStreamFactory) throws IOException {
        ChunkerFactory factory = ChunkerFactory.create(null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<ChunkSample> sampleStream = new ChunkSampleStream(lineStream)) {
            return ChunkerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
