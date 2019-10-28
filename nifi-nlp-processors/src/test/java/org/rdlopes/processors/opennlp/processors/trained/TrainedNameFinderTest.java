package org.rdlopes.processors.opennlp.processors.trained;

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
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainedNameFinderTest extends AbstractPreTrainedProcessorTest<TrainedNameFinder> {

    public TrainedNameFinderTest() {
        super(TrainedNameFinder.class);
    }

    private void assertProcessorCanFind(String nameType,
                                        Consumer<List<String>> nameListAssertion,
                                        Consumer<List<Span>> spanListAssertion,
                                        Consumer<List<Double>> probabilitiesAssertion) {
        testRunner.setProperty(NAMEFIND_NAME_TYPE.descriptor, nameType);
        Map<String, String> attributes = new HashMap<>();
        TOKENIZE_TOKEN_LIST.updateAttributesWithJson(attributes, SAMPLE_TOKENS_VINKEN);
        testRunner.enqueue(SAMPLE_CONTENT_VINKEN, attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TAGPOS_TAG_LIST.key, attributes.get(TAGPOS_TAG_LIST.key));
        flowFile.assertAttributeEquals(TOKENIZE_TOKEN_LIST.key, attributes.get(TOKENIZE_TOKEN_LIST.key));

        flowFile.assertAttributeExists(NAMEFIND_NAME_LIST.key);
        flowFile.assertAttributeExists(NAMEFIND_SPAN_LIST.key);
        flowFile.assertAttributeExists(NAMEFIND_PROBABILITIES.key);

        List<String> nameList = NAMEFIND_NAME_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<String>>() {});
        List<Span> nameSpans = NAMEFIND_SPAN_LIST.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Span>>() {});
        List<Double> probabilities = NAMEFIND_PROBABILITIES.getAsJSONFrom(flowFile.getAttributes(), new TypeToken<List<Double>>() {});

        nameListAssertion.accept(nameList);
        spanListAssertion.accept(nameSpans);
        probabilitiesAssertion.accept(probabilities);
    }

    @Test
    public void shouldFindDates() {
        setModelFilePath("/models/en-ner-date.bin");
        assertProcessorCanFind("date",
                               nameList -> assertThat(nameList).containsExactly("Nov . 29"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(15, 18, "date")),
                               probabilities -> assertThat(probabilities).hasSize(61));
    }

    @Test
    public void shouldFindLocations() {
        setModelFilePath("/models/en-ner-location.bin");
        assertProcessorCanFind("location",
                               nameList -> assertThat(nameList).containsExactly("N . V ."),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(26, 30, "location")),
                               probabilities -> assertThat(probabilities).hasSize(61));
    }

    @Test
    public void shouldFindOrganizations() {
        setModelFilePath("/models/en-ner-organization.bin");
        assertProcessorCanFind("organization",
                               nameList -> assertThat(nameList).containsExactly("Consolidated Gold Fields PLC"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(46, 50, "organization")),
                               probabilities -> assertThat(probabilities).hasSize(61));
    }

    @Test
    public void shouldFindPersons() {
        setModelFilePath("/models/en-ner-person.bin");
        assertProcessorCanFind("person",
                               nameList -> assertThat(nameList).containsExactly("Pierre Vinken", "Vinken", "Rudolph Agnew"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(0, 2, "person"),
                                                                                  new Span(21, 22, "person"),
                                                                                  new Span(36, 38, "person")),
                               probabilities -> assertThat(probabilities).hasSize(61));
    }

}
