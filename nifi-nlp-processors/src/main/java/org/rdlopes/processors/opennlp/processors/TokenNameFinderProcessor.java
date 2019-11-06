package org.rdlopes.processors.opennlp.processors;

import org.apache.nifi.annotation.behavior.*;

import java.lang.annotation.*;

import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_ALLOWED;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@InputRequirement(INPUT_ALLOWED)
@ReadsAttributes({@ReadsAttribute(attribute = TOKENIZER_TOKENS_LIST_KEY, description = TOKENIZER_TOKENS_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = TOKEN_NAME_FINDER_NAMES_LIST_KEY, description = TOKEN_NAME_FINDER_NAMES_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = TOKEN_NAME_FINDER_NAMES_SPAN_KEY, description = TOKEN_NAME_FINDER_NAMES_SPAN_DESCRIPTION)})
public @interface TokenNameFinderProcessor {
}
