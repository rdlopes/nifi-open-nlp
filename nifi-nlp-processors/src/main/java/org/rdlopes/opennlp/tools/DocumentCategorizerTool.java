package org.rdlopes.opennlp.tools;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.doccat.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.opennlp.common.NLPAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

public class DocumentCategorizerTool extends NLPTool<DoccatModel> {
    public DocumentCategorizerTool(Path modelPath, ComponentLog logger) {
        super(DoccatModel.class, modelPath, logger);
    }

    @Override
    protected void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, DoccatModel model, Map<String, String> evaluation) {
        DocumentCategorizer documentCategorizer = new DocumentCategorizerME(model);
        String[] sentencesList = NLPAttribute.get(NLPAttribute.SENTENCE_DETECTOR_SENTENCES_LIST_KEY, attributes, new TypeToken<String[]>() {
        });
        double[] results = documentCategorizer.categorize(sentencesList);

        String bestCategory = documentCategorizer.getBestCategory(results);
        List<String> categoryList = IntStream.range(0, documentCategorizer.getNumberOfCategories())
                .sequential()
                .mapToObj(documentCategorizer::getCategory)
                .collect(toList());
        Map<Double, Set<String>> scoreMap = documentCategorizer.sortedScoreMap(sentencesList);

        NLPAttribute.set(NLPAttribute.DOCUMENT_CATEGORIZER_CATEGORIES_LIST_KEY, evaluation, categoryList);
        NLPAttribute.set(NLPAttribute.DOCUMENT_CATEGORIZER_CATEGORIES_BEST_KEY, evaluation, bestCategory);
        NLPAttribute.set(NLPAttribute.DOCUMENT_CATEGORIZER_SCORE_MAP_KEY, evaluation, scoreMap);
    }

    @Override
    protected DoccatModel trainModel(ValidationContext validationContext,
                                     InputStreamFactory inputStreamFactory,
                                     TrainingParameters trainingParameters,
                                     String trainingLanguage) throws IOException {
        DoccatFactory factory = new DoccatFactory(new FeatureGenerator[]{new BagOfWordsFeatureGenerator(), new NGramFeatureGenerator()});
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, UTF_8);
             ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream)) {
            return DocumentCategorizerME.train(trainingLanguage, sampleStream, trainingParameters, factory);
        }
    }

}
