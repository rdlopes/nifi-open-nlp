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
@WritesAttributes({@WritesAttribute(attribute = SENTENCE_DETECTOR_SENTENCES_LIST_KEY, description = SENTENCE_DETECTOR_SENTENCES_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = SENTENCE_DETECTOR_SENTENCES_SPAN_KEY, description = SENTENCE_DETECTOR_SENTENCES_SPAN_DESCRIPTION),})
public @interface SentenceDetectorProcessor {
}
