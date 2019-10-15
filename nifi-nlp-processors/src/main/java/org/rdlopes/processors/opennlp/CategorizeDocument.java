package org.rdlopes.processors.opennlp;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.doccat.*;
import opennlp.tools.util.*;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.ATTRIBUTE_NLP_ERROR_DESCRIPTION;
import static org.rdlopes.processors.opennlp.CategorizeDocument.*;

@NlpProcessor
@Tags({"apache", "nlp", "document", "categorizer"})
@CapabilityDescription("Enriches flow file attributes with information about document category, as found by NLP engine.")
@WritesAttributes({@WritesAttribute(attribute = ATTRIBUTE_NLP_ERROR, description = ATTRIBUTE_NLP_ERROR_DESCRIPTION),
                   @WritesAttribute(attribute = ATTRIBUTE_DOCCAT_CATEGORY_BEST, description = "Holds the best category name found by trained model."),
                   @WritesAttribute(attribute = ATTRIBUTE_DOCCAT_CATEGORY_COUNT, description = "Holds the category count for the categories found by the trained model."),
                   @WritesAttribute(attribute = ATTRIBUTE_DOCCAT_CATEGORY_LIST, description = "Holds the list of categories found by the trained model, as a JSON list."),
                   @WritesAttribute(attribute = ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP, description = "Holds the results of evaluating content with the trained model, " +
                                                                                                 "as a <double, list<string>> JSON map.")})
@EqualsAndHashCode(callSuper = true)
public class CategorizeDocument extends AbstractNlpProcessor<DoccatModel> {

    static final String ATTRIBUTE_DOCCAT_CATEGORY_BEST = "nlp.doccat.category.best";

    static final String ATTRIBUTE_DOCCAT_CATEGORY_COUNT = "nlp.doccat.category.count";

    static final String ATTRIBUTE_DOCCAT_CATEGORY_LIST = "nlp.doccat.category.list";

    static final String ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP = "nlp.doccat.sorted.score.map";

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(Stream.of(PROPERTY_TRAINING_LANGUAGE),
                                                                                        super.getSupportedPropertyDescriptors().stream())
                                                                                .collect(toList());

    public CategorizeDocument() {super(DoccatModel.class);}

    @Override
    protected Map<String, String> doEvaluate(ProcessContext context, String content, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();
        DocumentCategorizer documentCategorizer = new DocumentCategorizerME(getModel());
        String[] splitContent = content.split("\\n");
        double[] results = documentCategorizer.categorize(splitContent);

        String bestCategory = documentCategorizer.getBestCategory(results);
        int categoriesCount = documentCategorizer.getNumberOfCategories();
        List<String> categoryList = IntStream.range(0, categoriesCount)
                                             .mapToObj(documentCategorizer::getCategory)
                                             .collect(toList());
        Map<Double, Set<String>> probabilities = documentCategorizer.sortedScoreMap(splitContent);

        evaluation.put(ATTRIBUTE_DOCCAT_CATEGORY_BEST, bestCategory);
        evaluation.put(ATTRIBUTE_DOCCAT_CATEGORY_COUNT, String.valueOf(categoriesCount));
        evaluation.put(ATTRIBUTE_DOCCAT_CATEGORY_LIST, new Gson().toJson(categoryList));
        evaluation.put(ATTRIBUTE_DOCCAT_SORTED_SCORE_MAP, new Gson().toJson(probabilities));

        return evaluation;
    }

    private DoccatModel trainModelFrom(ValidationContext validationContext, TrainingParameters trainingParameters, Charset charset, InputStreamFactory inputStreamFactory) throws IOException {
        final String trainingLanguage = validationContext.getProperty(PROPERTY_TRAINING_LANGUAGE).evaluateAttributeExpressions().getValue();
        DoccatFactory factory = new DoccatFactory(new FeatureGenerator[]{new BagOfWordsFeatureGenerator(), new NGramFeatureGenerator()});
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream)) {
            return DocumentCategorizerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }

    @Override
    protected DoccatModel trainModelFromData(ValidationContext validationContext, TrainingParameters trainingParameters, Charset charset, String trainingData) throws IOException {
        return trainModelFrom(validationContext, trainingParameters, charset, () -> toInputStream(trainingData, charset));
    }

    @Override
    protected DoccatModel trainModelFromFile(ValidationContext validationContext, TrainingParameters trainingParameters, Charset charset, File dataFile) throws IOException {
        return trainModelFrom(validationContext, trainingParameters, charset, new MarkableFileInputStreamFactory(dataFile));
    }
}
