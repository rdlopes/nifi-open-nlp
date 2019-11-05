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
@ReadsAttributes({@ReadsAttribute(attribute = SENTENCE_DETECTOR_SENTENCES_LIST_KEY, description = SENTENCE_DETECTOR_SENTENCES_LIST_DESCRIPTION)})
@WritesAttributes({@WritesAttribute(attribute = DOCUMENT_CATEGORIZER_CATEGORIES_LIST_KEY, description = DOCUMENT_CATEGORIZER_CATEGORIES_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = DOCUMENT_CATEGORIZER_CATEGORIES_BEST_KEY, description = DOCUMENT_CATEGORIZER_CATEGORIES_BEST_DESCRIPTION),
                   @WritesAttribute(attribute = DOCUMENT_CATEGORIZER_SCORE_MAP_KEY, description = DOCUMENT_CATEGORIZER_SCORE_MAP_DESCRIPTION)})
public @interface DocumentCategorizerProcessor {
}
