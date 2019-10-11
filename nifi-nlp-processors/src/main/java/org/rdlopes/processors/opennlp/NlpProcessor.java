package org.rdlopes.processors.opennlp;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.SupportsBatching;

import java.lang.annotation.*;

import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_REQUIRED;

/**
 * <p>
 * Annotation placed on an {@link AbstractNlpProcessor} to inherit grouped annotations.
 * </p>
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EventDriven
@SupportsBatching
@InputRequirement(INPUT_REQUIRED)
@interface NlpProcessor {
}
