package org.rdlopes.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.NLPAttribute;
import org.rdlopes.opennlp.common.NLPProperty;
import org.rdlopes.opennlp.processors.NLPProcessor;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class PreTrainedTokenNameFinderTest extends PreTrainedProcessorTest<PreTrainedTokenNameFinder> {
    public PreTrainedTokenNameFinderTest() {
        super(PreTrainedTokenNameFinder.class, "/models/en-ner-person.bin");
    }

    private void assertProcessorCanFind(String nameType,
                                        Consumer<List<String>> nameListAssertion,
                                        Consumer<List<Span>> spanListAssertion) {

        testRunner.setProperty(NLPProperty.NAMEFIND_NAME_TYPE.descriptor, nameType);
        testRunner.assertValid();

        Map<String, String> attributes = new HashMap<>();
        NLPAttribute.set(NLPAttribute.TOKENIZER_TOKENS_LIST_KEY, attributes, SAMPLE_TOKENS_SIMPLE);
        testRunner.enqueue(SAMPLE_CONTENT, attributes);
        testRunner.run();
        testRunner.assertTransferCount(NLPProcessor.RELATIONSHIP_UNMATCHED, 0);
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
    public void shouldFindDates() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-ner-date.bin").toString());
        assertProcessorCanFind("date",
                nameList -> assertThat(nameList).containsExactly("Nov . 29 th"),
                nameSpans -> assertThat(nameSpans).containsExactly(new Span(42, 46, "date")));
    }

    @Test
    public void shouldFindLocations() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-ner-location.bin").toString());
        assertProcessorCanFind("location",
                nameList -> assertThat(nameList).containsExactly("N . V .", "the Netherlands", "Belgium", "N . V"),
                nameSpans -> assertThat(nameSpans).containsExactly(
                        new Span(54, 58, "location"), new Span(71, 73, "location"), new Span(77, 78, "location"),
                        new Span(80, 83, "location")));
    }

    @Test
    public void shouldFindMoney() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-ner-money.bin").toString());
        assertProcessorCanFind("money",
                nameList -> assertThat(nameList).containsExactly("$ 800 . 000"),
                nameSpans -> assertThat(nameSpans).containsExactly(new Span(99, 103, "money")));
    }

    @Test
    public void shouldFindOrganizations() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-ner-organization.bin").toString());
        assertProcessorCanFind("organization",
                nameList -> assertThat(nameList).containsExactly("Consolidated Gold Fields PLC"),
                nameSpans -> assertThat(nameSpans).containsExactly(new Span(127, 131, "organization")));
    }

    @Test
    public void shouldFindPercentage() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-ner-percentage.bin").toString());
        assertProcessorCanFind("percentage",
                nameList -> assertThat(nameList).containsExactly("40 %", "10 %", "51 %"),
                nameSpans -> assertThat(nameSpans).containsExactly(
                        new Span(65, 67, "percentage"), new Span(74, 76, "percentage"), new Span(86, 88, "percentage")));
    }

    @Test
    public void shouldFindPersons() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-ner-person.bin").toString());
        assertProcessorCanFind("person",
                nameList -> assertThat(nameList).containsExactly("Pierre Vinken", "Vinken", "Elsevier N", "Rudolph Agnew"),
                nameSpans -> assertThat(nameSpans).containsExactly(new Span(25, 27, "person"),
                        new Span(49, 50, "person"),
                        new Span(79, 81, "person"),
                        new Span(117, 119, "person")
                ));
    }

    @Test
    public void shouldFindTime() throws URISyntaxException {
        testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-ner-time.bin").toString());
        assertProcessorCanFind("time",
                nameList -> assertThat(nameList).containsExactly("10 : 30", "7 : 00"),
                nameSpans -> assertThat(nameSpans).containsExactly(new Span(10, 13, "time"),
                        new Span(20, 23, "time")));
    }

}
