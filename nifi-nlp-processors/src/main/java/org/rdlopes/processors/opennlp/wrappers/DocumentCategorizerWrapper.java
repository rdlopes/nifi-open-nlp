package org.rdlopes.processors.opennlp.wrappers;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.doccat.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;

public class DocumentCategorizerWrapper extends NLPToolWrapper<DoccatModel> {

    public DocumentCategorizerWrapper() {
        super(DoccatModel.class);
    }

    @Override
    public void evaluateContent(ProcessContext context, DoccatModel model, String content, Map<String, String> attributes) {
        DocumentCategorizer documentCategorizer = new DocumentCategorizerME(model);
        String[] sentencesList = SENTDET_CHUNK_LIST.getAsJSONFrom(attributes, new TypeToken<String[]>() {});
        double[] results = documentCategorizer.categorize(sentencesList);

        String bestCategory = documentCategorizer.getBestCategory(results);
        List<String> categoryList = IntStream.range(0, documentCategorizer.getNumberOfCategories())
                                             .sequential()
                                             .mapToObj(documentCategorizer::getCategory)
                                             .collect(toList());
        Map<Double, Set<String>> probabilities = documentCategorizer.sortedScoreMap(sentencesList);

        DOCCAT_CATEGORY_LIST.updateAttributesWithJson(attributes, categoryList);
        DOCCAT_CATEGORY_BEST.updateAttributesWithString(attributes, bestCategory);
        DOCCAT_SCORE_MAP.updateAttributesWithJson(attributes, probabilities);
    }

    @Override
    public DoccatModel trainModel(PropertyContext propertyContext,
                                  String trainingLanguage,
                                  Charset charset,
                                  TrainingParameters trainingParameters,
                                  InputStreamFactory inputStreamFactory) throws IOException {
        DoccatFactory factory = new DoccatFactory(new FeatureGenerator[]{new BagOfWordsFeatureGenerator(), new NGramFeatureGenerator()});
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, charset);
             ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream)) {
            return DocumentCategorizerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }
}
