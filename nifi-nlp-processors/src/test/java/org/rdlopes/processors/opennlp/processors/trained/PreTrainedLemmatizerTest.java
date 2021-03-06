package org.rdlopes.processors.opennlp.processors.trained;

import com.google.gson.reflect.TypeToken;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class PreTrainedLemmatizerTest extends PreTrainedProcessorTest<PreTrainedLemmatizer> {
    public PreTrainedLemmatizerTest() {
        super(PreTrainedLemmatizer.class);
    }

    @Test
    public void shouldLemmatizeOpenNLPExample() throws URISyntaxException {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getFilePath("/models/en-lemmatizer.bin").toString());
        Map<String, String> attributes = new HashMap<>();
        set(POS_TAGGER_TAGS_LIST_KEY, attributes, SAMPLE_TAGS_SIMPLE);
        set(TOKENIZER_TOKENS_LIST_KEY, attributes, SAMPLE_TOKENS_SIMPLE);
        testRunner.enqueue(SAMPLE_CONTENT, attributes);
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
        flowFile.assertAttributeEquals(POS_TAGGER_TAGS_LIST_KEY, attributes.get(POS_TAGGER_TAGS_LIST_KEY));
        flowFile.assertAttributeEquals(TOKENIZER_TOKENS_LIST_KEY, attributes.get(TOKENIZER_TOKENS_LIST_KEY));

        flowFile.assertAttributeExists(LEMMATIZER_LEMMAS_LIST_KEY);
        List<String> lemmaList = get(LEMMATIZER_LEMMAS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});

        assertThat(lemmaList).containsExactly(
                "==", "please", "notice", "that", "this", "announcement", "will", "be", "update", "at", "10", ":", "30", "am", ",", "3", ":", "00", "pm", "and", "7", ":", "00", "pm", "==", "pierre",
                "vinken", ",", "61", "year", "old", ",", "will", "join", "the", "board", "a", "a", "non", "-", "executive", "director", "nov", ".", "29", "th", ".", "mr", ".", "vinken", "be",
                "chairman", "of", "elsevier", "n", ".", "v", ".", ",", "the", "dutch", "publishing", "group", "that", "own", "40", "%", "of", "publish", "magazine", "in", "the", "netherland", "and",
                "10", "%", "in", "belgium", ".", "elsevier", "n", ".", "v", ".", "now", "represent", "51", "%", "of", "the", "total", "capital", "of", "the", "company", ",", "worth", "many", "than",
                "$", "800", ".", "000", "(", "1", ".", "000", ".", "000", "euro", ",", "900", ".", "000", "pound", ")", ".", "rudolph", "agnew", ",", "55", "year", "old", "and", "former", "chairman",
                "of", "consolidated", "gold", "field", "plc", ",", "be", "name", "a", "director", "of", "this", "british", "industrial", "conglomerate", ".");
    }

}
