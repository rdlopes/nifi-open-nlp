package org.rdlopes.opennlp.common;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;

public enum TokenizerType {
    WHITESPACE(WhitespaceTokenizer.INSTANCE),
    SIMPLE(SimpleTokenizer.INSTANCE);

    public final Tokenizer tokenizer;

    TokenizerType(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }
}
