package org.rdlopes.processors.opennlp.processors;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;

import java.lang.annotation.*;

import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_REQUIRED;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@InputRequirement(INPUT_REQUIRED)
@WritesAttributes({@WritesAttribute(attribute = LANGUAGE_DETECTOR_LANGUAGES_LIST_KEY, description = LANGUAGE_DETECTOR_LANGUAGES_LIST_DESCRIPTION),
                   @WritesAttribute(attribute = LANGUAGE_DETECTOR_LANGUAGES_BEST_KEY, description = LANGUAGE_DETECTOR_LANGUAGES_BEST_DESCRIPTION),
                   @WritesAttribute(attribute = LANGUAGE_DETECTOR_SUPPORTED_LIST_KEY, description = LANGUAGE_DETECTOR_SUPPORTED_LIST_DESCRIPTION)})
public @interface LanguageDetectorProcessor {
}
