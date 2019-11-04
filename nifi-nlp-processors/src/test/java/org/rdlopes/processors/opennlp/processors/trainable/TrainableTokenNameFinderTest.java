package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.*;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableTokenNameFinderTest extends TrainableProcessorTest<TrainableTokenNameFinder> {

    public TrainableTokenNameFinderTest() {
        super(TrainableTokenNameFinder.class);
    }

    private void assertProcessorCanFind(String nameType,
                                        String[] tokens,
                                        String data,
                                        Consumer<List<String>> nameListAssertion,
                                        Consumer<List<Span>> spanListAssertion) {
        testRunner.setProperty(NAMEFIND_NAME_TYPE.descriptor, nameType);
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, tokens);

        testRunner.enqueue(data, attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TAGPOS_TAG_LIST.key, attributes.get(TAGPOS_TAG_LIST.key));
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));

        flowFile.assertAttributeExists(NAMEFIND_NAME_LIST.key);
        flowFile.assertAttributeExists(NAMEFIND_SPAN_LIST.key);

        List<String> nameList = NAMEFIND_NAME_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> nameSpans = NAMEFIND_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        nameListAssertion.accept(nameList);
        spanListAssertion.accept(nameSpans);
    }

    @Test
    public void shouldFindNames() {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getClass().getResource("/training/en-namefind.train").getFile());
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_CUTOFF.descriptor, String.valueOf(1));
        testRunner.setProperty(TRAINABLE_TRAINING_PARAM_ITERATIONS.descriptor, String.valueOf(70));
        assertProcessorCanFind("default",
                               new String[]{"Hi", "Mike", ",", "it's", "Stefanie", "Schmidt", "."},
                               "",
                               names -> assertThat(names).containsExactly("Mike", "Stefanie Schmidt"),
                               spans -> assertThat(spans).containsExactly(new Span(1, 2, "default"), new Span(4, 6, "default")));
    }

}
