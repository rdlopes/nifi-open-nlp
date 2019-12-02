package org.rdlopes.processors.opennlp.processors.trainable;

import com.google.gson.reflect.TypeToken;
import opennlp.tools.util.Span;
import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINABLE_TRAINING_FILE_PATH;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.processors.opennlp.processors.NLPProcessor.RELATIONSHIP_UNMATCHED;

public class TrainableTokenizerTest extends TrainableProcessorTest<TrainableTokenizer> {

    public TrainableTokenizerTest() {
        super(TrainableTokenizer.class);
    }

    @Test
    public void shouldTokenize() throws URISyntaxException {
        testRunner.setProperty(TRAINABLE_TRAINING_FILE_PATH.descriptor, getFilePath("/training/en-token.train").toString());
        testRunner.assertValid();

        testRunner.enqueue("Sounds like it's not properly thought through!");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();

        flowFile.assertAttributeExists(TOKENIZER_TOKENS_LIST_KEY);
        List<String> tokensList = get(TOKENIZER_TOKENS_LIST_KEY, flowFile.getAttributes(), new TypeToken<List<String>>() {});
        flowFile.assertAttributeExists(TOKENIZER_TOKENS_SPAN_KEY);
        List<Span> tokenSpans = get(TOKENIZER_TOKENS_SPAN_KEY, flowFile.getAttributes(), new TypeToken<List<Span>>() {});

        assertThat(tokensList).containsExactly("Sounds", "like", "it", "'s", "not", "properly", "thought", "through", "!");
        assertThat(tokenSpans).containsExactly(
                new Span(0, 6, null), new Span(7, 11, null), new Span(12, 14, null), new Span(14, 16, null), new Span(17, 20, null),
                new Span(21, 29, null), new Span(30, 37, null), new Span(38, 45, null), new Span(45, 46, null)
        );
    }

}
