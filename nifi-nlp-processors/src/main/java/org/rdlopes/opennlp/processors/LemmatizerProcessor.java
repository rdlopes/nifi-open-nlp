package org.rdlopes.opennlp.processors;

import org.apache.nifi.annotation.behavior.*;

import java.lang.annotation.*;

import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_ALLOWED;
import static org.rdlopes.opennlp.common.NLPAttribute.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@InputRequirement(INPUT_ALLOWED)
@ReadsAttributes({@ReadsAttribute(attribute = TOKENIZER_TOKENS_LIST_KEY, description = TOKENIZER_TOKENS_LIST_DESCRIPTION),
                  @ReadsAttribute(attribute = POS_TAGGER_TAGS_LIST_KEY, description = POS_TAGGER_TAGS_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = LEMMATIZER_LEMMAS_LIST_KEY, description = LEMMATIZER_LEMMAS_LIST_DESCRIPTION)})
public @interface LemmatizerProcessor {
}
