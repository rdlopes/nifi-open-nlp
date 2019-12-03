package org.rdlopes.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.chunker.*;
import opennlp.tools.util.*;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.opennlp.common.NLPAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public class ChunkerTool extends NLPTool<ChunkerModel> {
    public ChunkerTool(Path modelPath, ComponentLog logger) {
        super(ChunkerModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, ChunkerModel model, Map<String, String> evaluation) {
        String[] tagsList = NLPAttribute.get(NLPAttribute.POS_TAGGER_TAGS_LIST_KEY, attributes, new TypeToken<String[]>() {
        });
        String[] tokensList = NLPAttribute.get(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, new TypeToken<String[]>() {
        });

        Chunker chunker = new ChunkerME(model);
        String[] chunkList = chunker.chunk(tokensList, tagsList);
        Span[] spanList = chunker.chunkAsSpans(tokensList, tagsList);

        NLPAttribute.set(NLPAttribute.CHUNKER_CHUNKS_LIST_KEY, evaluation, chunkList);
        NLPAttribute.set(NLPAttribute.CHUNKER_CHUNKS_SPAN_KEY, evaluation, spanList);
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
