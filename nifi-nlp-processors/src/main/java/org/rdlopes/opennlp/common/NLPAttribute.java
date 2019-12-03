package org.rdlopes.opennlp.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;

import java.util.Map;

@UtilityClass
public final class NLPAttribute {
    public static final String CHUNKER_CHUNKS_LIST_DESCRIPTION = "Chunks found in content, as a JSON String list.";

    public static final String CHUNKER_CHUNKS_LIST_KEY = "nlp.chunker.chunks.list";

    public static final String CHUNKER_CHUNKS_SPAN_DESCRIPTION = "Span of chunks found in content, as a JSON Span list.";

    public static final String CHUNKER_CHUNKS_SPAN_KEY = "nlp.chunker.chunks.span";

    public static final String DOCUMENT_CATEGORIZER_CATEGORIES_BEST_DESCRIPTION = "Best category found in content, probabilities wise";

    public static final String DOCUMENT_CATEGORIZER_CATEGORIES_BEST_KEY = "nlp.document.categorizer.categories.best";

    public static final String DOCUMENT_CATEGORIZER_CATEGORIES_LIST_DESCRIPTION = "Categories found in content, as a JSON String list.";

    public static final String DOCUMENT_CATEGORIZER_CATEGORIES_LIST_KEY = "nlp.document.categorizer.categories.list";

    public static final String DOCUMENT_CATEGORIZER_SCORE_MAP_DESCRIPTION = "A map associating descending probabilities and the corresponding categories, as a JSON Map<Double>, Set<String>>.";

    public static final String DOCUMENT_CATEGORIZER_SCORE_MAP_KEY = "nlp.document.categorizer.score.map";

    public static final String LANGUAGE_DETECTOR_LANGUAGES_BEST_DESCRIPTION = "Best language found in content, probabilities wise";

    public static final String LANGUAGE_DETECTOR_LANGUAGES_BEST_KEY = "nlp.language.detector.languages.best";

    public static final String LANGUAGE_DETECTOR_LANGUAGES_LIST_DESCRIPTION = "Languages found in content, as a JSON Language list.";

    public static final String LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY = "nlp.language.detector.languages.list";

    public static final String LANGUAGE_DETECTOR_SUPPORTED_LIST_DESCRIPTION = "Languages supported by the model, as a JSON String list.";

    public static final String LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY = "nlp.language.detector.supported.list";

    public static final String LEMMATIZER_LEMMAS_LIST_DESCRIPTION = "Lemmas found in content, as a JSON String list";

    public static final String LEMMATIZER_LEMMAS_LIST_KEY = "nlp.lemmatizer.lemmas.list";

    public static final String NLP_EVALUATION_ERROR_DESCRIPTION = "Error message raised by processing the content, if any.";

    public static final String NLP_EVALUATION_ERROR_KEY = "nlp.evaluation.error";

    public static final String PARSER_PARSES_LIST_DESCRIPTION = "Parses found in content, as a JSON String list.";

    public static final String PARSER_PARSES_LIST_KEY = "nlp.parser.parses.list";

    public static final String POS_TAGGER_TAGS_LIST_DESCRIPTION = "Tags found in content, as a JSON String list.";

    public static final String POS_TAGGER_TAGS_LIST_KEY = "nlp.pos.tagger.tags.list";

    public static final String SENTENCE_DETECTOR_SENTENCES_LIST_DESCRIPTION = "Sentences found in content, as a JSON String list.";

    public static final String SENTENCE_DETECTOR_SENTENCES_LIST_KEY = "nlp.sentence.detector.sentences.list";

    public static final String SENTENCE_DETECTOR_SENTENCES_SPAN_DESCRIPTION = "Span of sentences found in content, as a JSON Span list.";

    public static final String SENTENCE_DETECTOR_SENTENCES_SPAN_KEY = "nlp.sentence.detector.sentences.span";

    public static final String TOKENIZER_TOKENS_LIST_DESCRIPTION = "Tokens found in content, as a JSON String list.";

    public static final String TOKENIZER_TOKENS_LIST_KEY = "nlp.tokenizer.tokens.list";

    public static final String TOKENIZER_TOKENS_SPAN_DESCRIPTION = "Span of tokens found in content, as a JSON Span list.";

    public static final String TOKENIZER_TOKENS_SPAN_KEY = "nlp.tokenizer.tokens.span";

    public static final String TOKEN_NAME_FINDER_NAMES_LIST_DESCRIPTION = "Names found in content, as a JSON String list.";

    public static final String TOKEN_NAME_FINDER_NAMES_LIST_KEY = "nlp.token.name.finder.names.list";

    public static final String TOKEN_NAME_FINDER_NAMES_SPAN_DESCRIPTION = "Span of names found in content, as a JSON Span list.";

    public static final String TOKEN_NAME_FINDER_NAMES_SPAN_KEY = "nlp.token.name.finder.names.span";

    private static final Gson gson = new GsonBuilder().create();

    public static <T> T get(String attribute, Map<String, String> attributes, TypeToken<T> type) {
        return new Gson().fromJson(attributes.get(attribute), type.getType());
    }

    public static void set(String attribute, Map<String, String> attributes, Object content) {
        attributes.put(attribute, gson.toJson(content));
    }

    public static void set(String attribute, Map<String, String> attributes, String content) {
        attributes.put(attribute, content);
    }

    public static FlowFile set(String attribute, ProcessSession session, FlowFile flowFile, String content) {
        return session.putAttribute(flowFile, attribute, content);
    }
}
