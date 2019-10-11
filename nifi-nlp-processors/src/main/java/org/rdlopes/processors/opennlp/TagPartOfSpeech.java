package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.postag.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.TagPartOfSpeech.*;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION;

@NlpProcessor
@Tags({"apache", "nlp", "tag", "POS", "part", "speech"})
@CapabilityDescription("Enriches the content of a flow file with tags.")
@ReadsAttributes({@ReadsAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_LIST, description = ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_TAGPOS_TAG_COUNT, description = "The number of tags found in the flow file content."),
                   @WritesAttribute(attribute = ATTRIBUTE_TAGPOS_TAG_LIST, description = ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION)})
@EqualsAndHashCode(callSuper = true)
public class TagPartOfSpeech extends AbstractNlpProcessor<POSModel> {

    static final String ATTRIBUTE_TAGPOS_TAG_COUNT = "nlp.tagpos.tag.count";

    static final String ATTRIBUTE_TAGPOS_TAG_LIST = "nlp.tagpos.tag.list";

    static final String ATTRIBUTE_TAGPOS_TAG_LIST_DESCRIPTION = "The list of tags found by the TagPOS tool, as a JSON list.";

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(Stream.of(PROPERTY_TRAINING_LANGUAGE),
                                                                                        super.getSupportedPropertyDescriptors().stream())
                                                                                .collect(toList());

    public TagPartOfSpeech() {super(POSModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, String content, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();
        POSTagger tagger = new POSTaggerME(getModel());
        String[] tokensList = attributeAsStringArray(attributes.get(ATTRIBUTE_TOKENIZE_TOKEN_LIST));

        String[] tagsList = tagger.tag(tokensList);

        evaluation.put(ATTRIBUTE_TAGPOS_TAG_COUNT, String.valueOf(tagsList.length));
        evaluation.put(ATTRIBUTE_TAGPOS_TAG_LIST, new Gson().toJson(tagsList));

        return evaluation;
    }

    @Override
    protected POSModel doTrain(ValidationContext context, TrainingParameters parameters, Charset charset, ObjectStream<String> stream) throws IOException {
        final String trainingLanguage = context.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        POSTaggerFactory factory = new POSTaggerFactory();
        try (ObjectStream<POSSample> sampleStream = new WordTagSampleStream(stream)) {
            return POSTaggerME.train(trainingLanguage, sampleStream, parameters, factory);
        }
    }
}
