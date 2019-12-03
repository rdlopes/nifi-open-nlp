package org.rdlopes.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.NLPAttribute;
import org.rdlopes.opennlp.common.NLPProperty;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainableTokenNameFinderTest extends TrainableProcessorTest<TrainableTokenNameFinder> {

    public TrainableTokenNameFinderTest() {
        super(TrainableTokenNameFinder.class);
    }

    private void assertProcessorCanFind(String[] tokens,
                                        String data,
                                        Consumer<List<String>> nameListAssertion,
                                        Consumer<List<Span>> spanListAssertion) {
        testRunner.setProperty(NLPProperty.NAMEFIND_NAME_TYPE.descriptor, "default");
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, tokens);

        testRunner.enqueue(data, attributes);
        testRunner.run();
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(BaseProcessor.RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(BaseProcessor.RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes.get(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(NLPAttribute.TOKEN_NAME_FINDER_NAMES_LIST_KEY);
        List<String> nameList = NLPAttribute.get(NLPAttribute.TOKEN_NAME_FINDER_NAMES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {
        });
        flowFile.assertAttributeExists(NLPAttribute.TOKEN_NAME_FINDER_NAMES_SPAN_KEY);
        List<Span> nameSpans = NLPAttribute.get(NLPAttribute.TOKEN_NAME_FINDER_NAMES_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {
        });

        nameListAssertion.accept(nameList);
        spanListAssertion.accept(nameSpans);
    }

    @Test
    public void shouldFindNames() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath("/training/en-namefind.train").toString());
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(1));
        testRunner.setProperty(NLPProperty.TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(70));
        assertProcessorCanFind(new String[]{"Hi", "Mike", ",", "it's", "Stefanie", "Schmidt", "."},
                "",
                names -> assertThat(names).containsExactly("Mike", "Stefanie Schmidt"),
                spans -> assertThat(spans).containsExactly(new Span(1, 2, "default"), new Span(4, 6, "default")));
    }

}
