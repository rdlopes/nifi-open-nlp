package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.processors.opennlp.processors.NLPProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.NAMEFIND_NAME_TYPE;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;

public class PreTrainedTokenNameFinderTest extends PreTrainedProcessorTest<PreTrainedTokenNameFinder> {
    public PreTrainedTokenNameFinderTest() {
        super(PreTrainedTokenNameFinder.class);
    }

    private void assertProcessorCanFind(String nameType,
                                        Consumer<List<String>> nameListAssertion,
                                        Consumer<List<Span>> spanListAssertion) {

        testRunner.setProperty(NAMEFIND_NAME_TYPE.descriptor, nameType);
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, SAMPLE_TOKENS_SIMPLE);
        testRunner.enqueue(SAMPLE_CONTENT, attributes);
        testRunner.run();
        testRunner.assertTransferCount(NLPProcessor.RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(TOKENIZER_TOKENS_LIST_KEY, attributes.get(TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(TOKEN_NAME_FINDER_NAMES_LIST_KEY);
        List<String> nameList = get(TOKEN_NAME_FINDER_NAMES_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        flowFile.assertAttributeExists(TOKEN_NAME_FINDER_NAMES_SPAN_KEY);
        List<Span> nameSpans = get(TOKEN_NAME_FINDER_NAMES_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        nameListAssertion.accept(nameList);
        spanListAssertion.accept(nameSpans);
    }

    @Test
    public void shouldFindDates() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-ner-date.bin").getFile());
        assertProcessorCanFind("date",
                               nameList -> assertThat(nameList).containsExactly("Nov . 29 th"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(42, 46, "date")));
    }

    @Test
    public void shouldFindLocations() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-ner-location.bin").getFile());
        assertProcessorCanFind("location",
                               nameList -> assertThat(nameList).containsExactly("N . V .", "the Netherlands", "Belgium", "N . V"),
                               nameSpans -> assertThat(nameSpans).containsExactly(
                                       new Span(54, 58, "location"), new Span(71, 73, "location"), new Span(77, 78, "location"),
                                       new Span(80, 83, "location")));
    }

    @Test
    public void shouldFindMoney() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-ner-money.bin").getFile());
        assertProcessorCanFind("money",
                               nameList -> assertThat(nameList).containsExactly("$ 800 . 000"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(99, 103, "money")));
    }

    @Test
    public void shouldFindOrganizations() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-ner-organization.bin").getFile());
        assertProcessorCanFind("organization",
                               nameList -> assertThat(nameList).containsExactly("Consolidated Gold Fields PLC"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(127, 131, "organization")));
    }

    @Test
    public void shouldFindPercentage() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-ner-percentage.bin").getFile());
        assertProcessorCanFind("percentage",
                               nameList -> assertThat(nameList).containsExactly("40 %", "10 %", "51 %"),
                               nameSpans -> assertThat(nameSpans).containsExactly(
                                       new Span(65, 67, "percentage"), new Span(74, 76, "percentage"), new Span(86, 88, "percentage")));
    }

    @Test
    public void shouldFindPersons() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-ner-person.bin").getFile());
        assertProcessorCanFind("person",
                               nameList -> assertThat(nameList).containsExactly("Pierre Vinken", "Vinken", "Elsevier N", "Rudolph Agnew"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(25, 27, "person"),
                                                                                  new Span(49, 50, "person"),
                                                                                  new Span(79, 81, "person"),
                                                                                  new Span(117, 119, "person")
                               ));
    }

    @Test
    public void shouldFindTime() {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource("/models/en-ner-time.bin").getFile());
        assertProcessorCanFind("time",
                               nameList -> assertThat(nameList).containsExactly("10 : 30", "7 : 00"),
                               nameSpans -> assertThat(nameSpans).containsExactly(new Span(10, 13, "time"),
                                                                                  new Span(20, 23, "time")));
    }

}
