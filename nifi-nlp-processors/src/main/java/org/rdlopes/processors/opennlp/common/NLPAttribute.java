package org.rdlopes.processors.opennlp.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;

import java.util.Map;

public enum NLPAttribute {
    COMMON_ERROR("nlp.common.error", "Error message raised by processing the content, if any."),
    // tokenizer
    TOKENIZE_TOKEN_LIST("nlp.tokenize.token.list", "The list of tokens as found in the content of the flow file."),
    TOKENIZE_SPAN_LIST("nlp.tokenize.span.list", "The list of tokens as found in the content of the flow file, as a JSON span list."),
    // POS tagger
    TAGPOS_TAG_LIST("nlp.tagpos.tag.list", "The list of tags found by the TagPOS tool, as a JSON list."),
    // chunker
    CHUNK_CHUNK_LIST("nlp.chunk.chunk.list", "The list of chunks found, as a JSON string list."),
    CHUNK_SPAN_LIST("nlp.chunk.span.list", "The list of chunks found, as a JSON span list."),
    // sentence detector
    SENTDET_CHUNK_LIST("nlp.sentdet.chunk.list", "Holds the sentence chunks list found in the flow file content."),
    SENTDET_SPAN_LIST("nlp.sentdet.span.list", "Holds the sentence chunks list found in the flow file content, as a JSON span list."),
    SENTDET_PROBABILITIES("nlp.sentdet.probabilities", "The engine probabilities for the parsing."),
    // document categorizer
    DOCCAT_CATEGORY_LIST("nlp.doccat.category.list", "Holds the list of categories found by the trained model, as a JSON list."),
    DOCCAT_CATEGORY_BEST("nlp.doccat.category.best", "Holds the best category name found by trained model."),
    DOCCAT_SCORE_MAP("nlp.doccat.score.map", "Holds the results of evaluating content with the trained model, as a <double, list<string>> JSON map."),
    // language detector
    LANGDET_PREDICTED_LANGUAGE("nlp.langdet.predicted.language", "Holds the language code predicted from flow file content."),
    LANGDET_CONFIDENCE("nlp.langdet.confidence", "Holds the confidence for the made prediction."),
    LANGDET_PROBABLE_LANGUAGE_LIST("nlp.langdet.probable.language.list", "Holds the probable languages for the flow file content, as a JSON languages list."),
    LANGDET_SUPPORTED_LANGUAGE_LIST("nlp.langdet.supported.language.list", "Holds the language list supported by NLP engine, as a JSON string list."),
    // lemmatizer
    LEMMATIZE_LEMMA_LIST("nlp.lemmatize.lemma.list", "Lemmas list as evaluated from flow file content."),
    LEMMATIZE_PREDICTED_LIST("nlp.lemmatize.predicted.list", "List of predicted lemmas."),
    LEMMATIZE_PREDICTED_SES_LIST("nlp.lemmatize.predicted.ses.list.size", "SES prediction list"),
    LEMMATIZE_PROBABILITIES("nlp.lemmatize.probabilities", "Lemmas probabilities, as a JSON list of doubles."),
    LEMMATIZE_TOPK_LIST("nlp.lemmatize.topk.list", "Top K lemmas list."),
    LEMMATIZE_TOPK_SEQUENCE_LIST("nlp.lemmatize.topk.sequence.list", "Top K sequence list."),
    // name finder
    NAMEFIND_NAME_LIST("nlp.namefind.name.list", "Holds  the list of names found in flow file content, as a JSON strings list."),
    NAMEFIND_SPAN_LIST("nlp.namefind.span.list", "Holds  the list of names spans found in flow file content, as a JSON span list."),
    NAMEFIND_PROBABILITIES("nlp.namefind.probabilities", "Holds probabilities for each span prediction from flow file content."),

    PARSER_PARSE_LIST("nlp.parser.parse.list", "Holds the list of parses found in flow file content.");

    private final String description;

    private final Gson gson = new Gson();

    public final String key;

    NLPAttribute(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public <T> T getAsJSONFrom(Map<String, String> attributes, TypeToken<T> type) {
        return new Gson().fromJson(attributes.get(key), type.getType());
    }

    public void updateAttributesWithJson(Map<String, String> attributes, Object content) {
        attributes.put(key, gson.toJson(content));
    }

    public void updateAttributesWithString(Map<String, String> attributes, Object value) {
        attributes.put(key, String.valueOf(value));
    }

    public FlowFile updateFlowFile(ProcessSession session, FlowFile flowFile, String value) {
        return session.putAttribute(flowFile, key, value);
    }
}
