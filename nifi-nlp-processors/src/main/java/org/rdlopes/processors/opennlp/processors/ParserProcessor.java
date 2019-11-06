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
@WritesAttributes({@WritesAttribute(attribute = PARSER_PARSES_LIST_KEY, description = PARSER_PARSES_LIST_DESCRIPTION)})
public @interface ParserProcessor {
}
