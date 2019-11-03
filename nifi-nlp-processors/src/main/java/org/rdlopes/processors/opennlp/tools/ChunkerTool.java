package org.rdlopes.processors.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.chunker.*;
import opennlp.tools.util.*;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;

public class ChunkerTool extends NLPTool<ChunkerModel> {
    public ChunkerTool(Path modelPath, ComponentLog logger) {
        super(ChunkerModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, ChunkerModel model, Map<String, String> evaluation) {
        String[] tagsList = TAGPOS_TAG_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});
        String[] tokensList = TOKENIZE_TOKEN_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});

        ChunkerME chunker = new ChunkerME(model);
        String[] chunkList = chunker.chunk(tokensList, tagsList);
        Span[] spanList = chunker.chunkAsSpans(tokensList, tagsList);

        CHUNK_CHUNK_LIST.updateAttributesWithJson(evaluation, chunkList);
        CHUNK_SPAN_LIST.updateAttributesWithJson(evaluation, spanList);
    }

    @Override
    protected ChunkerModel trainModel(ValidationContext validationContext,
                                      InputStreamFactory inputStreamFactory,
                                      TrainingParameters trainingParameters,
                                      String trainingLanguage) throws IOException {
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
             ObjectStream<ChunkSample> sampleStream = new ChunkSampleStream(lineStream)) {
            ChunkerFactory factory = ChunkerFactory.create(null);
            return ChunkerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
