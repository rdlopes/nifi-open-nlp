package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.namefind.*;
import opennlp.tools.util.*;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
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
import static opennlp.tools.util.Span.spansToStrings;
import static org.apache.nifi.expression.ExpressionLanguageScope.VARIABLE_REGISTRY;
import static org.apache.nifi.processor.util.StandardValidators.NON_BLANK_VALIDATOR;
import static org.rdlopes.processors.opennlp.FindNames.*;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST;
import static org.rdlopes.processors.opennlp.Tokenize.ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION;

@NlpProcessor
@Tags({"apache", "nlp", "names", "finder"})
@CapabilityDescription("Find names placed in the content of a flow file.")
@ReadsAttributes({@ReadsAttribute(attribute = ATTRIBUTE_TOKENIZE_TOKEN_LIST, description = ATTRIBUTE_TOKENIZE_TOKEN_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_NAMEFIND_NAME_LIST, description = "Holds  the list of names found in flow file content, as a JSON strings list."),
                   @WritesAttribute(attribute = ATTRIBUTE_NAMEFIND_NAME_SPANS, description = "Holds  the list of names spans found in flow file content, as a JSON span list."),
                   @WritesAttribute(attribute = ATTRIBUTE_NAMEFIND_PROBABILITIES, description = "Holds probabilities for each span prediction from flow file content.")})
@EqualsAndHashCode(callSuper = true)
public class FindNames extends AbstractNlpProcessor<TokenNameFinderModel> {

    static final String ATTRIBUTE_NAMEFIND_NAME_LIST = "nlp.namefind.name.list";

    static final String ATTRIBUTE_NAMEFIND_NAME_SPANS = "nlp.namefind.name.spans";

    static final String ATTRIBUTE_NAMEFIND_PROBABILITIES = "nlp.namefind.probabilities";

    static final PropertyDescriptor PROPERTY_NAME_TYPE = new PropertyDescriptor.Builder()
            .name("Name type")
            .description("The name type to look for (might depend on the model selected).")
            .required(true)
            .expressionLanguageSupported(VARIABLE_REGISTRY)
            .addValidator(NON_BLANK_VALIDATOR)
            .defaultValue("person")
            .build();

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(Stream.of(PROPERTY_TRAINING_LANGUAGE, PROPERTY_NAME_TYPE),
                                                                                        super.getSupportedPropertyDescriptors().stream())
                                                                                .collect(toList());

    public FindNames() {super(TokenNameFinderModel.class);}

    @Override
    protected Map<String, String> executeModel(ProcessContext context, String content, Map<String, String> attributes, TokenNameFinderModel model) {
        Map<String, String> evaluation = new HashMap<>();
        String[] tokensList = attributeAsStringArray(attributes.get(ATTRIBUTE_TOKENIZE_TOKEN_LIST));

        NameFinderME nameFinder = new NameFinderME(model);
        Span[] nameSpans = nameFinder.find(tokensList);
        String[] nameList = spansToStrings(nameSpans, tokensList);
        double[] probabilities = nameFinder.probs();

        evaluation.put(ATTRIBUTE_NAMEFIND_NAME_SPANS, new Gson().toJson(nameSpans));
        evaluation.put(ATTRIBUTE_NAMEFIND_NAME_LIST, new Gson().toJson(nameList));
        evaluation.put(ATTRIBUTE_NAMEFIND_PROBABILITIES, new Gson().toJson(probabilities));

        return evaluation;
    }

    @Override
    protected TokenNameFinderModel trainModel(ValidationContext validationContext,
                                              Collection<ValidationResult> results,
                                              TrainingParameters trainingParameters,
                                              Charset charset,
                                              InputStreamFactory inputStreamFactory) throws IOException {
        final String trainingLanguage = validationContext.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        final String nameType = validationContext.getProperty(PROPERTY_NAME_TYPE).evaluateAttributeExpressions().getValue();
        TokenNameFinderFactory factory = new TokenNameFinderFactory();
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream)) {
            return NameFinderME.train(trainingLanguage, nameType, sampleStream, trainingParameters, factory);
        }
    }
}
