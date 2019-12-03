package org.rdlopes.opennlp.processors;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;

import java.lang.annotation.*;

import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_REQUIRED;
import static org.rdlopes.opennlp.common.NLPAttribute.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@InputRequirement(INPUT_REQUIRED)
@WritesAttributes({@WritesAttribute(attribute = TOKENIZER_TOKENS_LIST_KEY, description = TOKENIZER_TOKENS_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = TOKENIZER_TOKENS_SPAN_KEY, description = TOKENIZER_TOKENS_SPAN_DESCRIPTION)})
public @interface TokenizerProcessor {
}
