package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.chunker.*;
import opennlp.tools.util.*;
import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR_DESCRIPTION;
import static org.rdlopes.processors.opennlp.Chunk.*;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION;

@NlpProcessor
@Tags({"apache", "nlp", "chunk"})
@CapabilityDescription("Enriches flow file attributes with information about document category, as found by NLP engine.")
@DynamicProperty(name = "Category name", value = "Sentence", expressionLanguageScope = VARIABLE_REGISTRY,
                 description = "Trains the model to include this category association.")
@ReadsAttributes({@ReadsAttribute(attribute = ATTRIBUTE_TAGPOS_TAG_LIST, description = ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION),
                  @ReadsAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_LIST, description = ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_CHUNK_COUNT, description = "Holds the chunks count found in the flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_CHUNK_LIST, description = "Holds the chunks list found in the flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_CHUNK_SPANS, description = "Holds the chunk list found in the flow file content, as a JSON spans list.")})
@EqualsAndHashCode(callSuper = true)
public class Chunk extends AbstractNlpProcessor<ChunkerModel> {

    static final String ATTRIBUTE_CHUNK_COUNT = "nlp.chunk.count";

    static final String ATTRIBUTE_CHUNK_LIST = "nlp.chunk.list";

    static final String ATTRIBUTE_CHUNK_SPANS = "nlp.chunk.spans";

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(Stream.of(PROPERTY_TRAINING_LANGUAGE),
                                                                                        super.getSupportedPropertyDescriptors().stream())
                                                                                .collect(toList());

    public Chunk() {super(ChunkerModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, String content, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();
        String[] tagsList = attributeAsStringArray(attributes.get(ATTRIBUTE_TAGPOS_TAG_LIST));
        String[] tokensList = attributeAsStringArray(attributes.get(ATTRIBUTE_TOKENIZE_TOKEN_LIST));

        ChunkerME chunker = new ChunkerME(getModel());

        String[] chunks = chunker.chunk(tokensList, tagsList);
        Span[] chunkAsSpans = chunker.chunkAsSpans(tokensList, tagsList);

        evaluation.put(ATTRIBUTE_CHUNK_COUNT, String.valueOf(chunks.length));
        evaluation.put(ATTRIBUTE_CHUNK_LIST, new Gson().toJson(chunks));
        evaluation.put(ATTRIBUTE_CHUNK_SPANS, new Gson().toJson(chunkAsSpans));

        return evaluation;
    }

    @Override
    protected ChunkerModel trainModel(ValidationContext validationContext,
                                      Collection<ValidationResult> results,
                                      TrainingParameters trainingParameters,
                                      Charset charset,
                                      InputStreamFactory inputStreamFactory) throws IOException {
        final String trainingLanguage = validationContext.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        ChunkerFactory factory = ChunkerFactory.create(null);
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<ChunkSample> sampleStream = new ChunkSampleStream(lineStream)) {
            return ChunkerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
