package org.rdlopes.opennlp.common;

import lombok.Getter;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public abstract class BaseProcessor extends AbstractProcessor {

    public static final Relationship RELATIONSHIP_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Parsing completed successfully")
            .build();

    public static final Relationship RELATIONSHIP_UNMATCHED = new Relationship.Builder()
            .name("unmatched")
            .description("Unmatched content")
            .build();

    @Getter
    private final Set<Relationship> relationships = Stream.of(
            RELATIONSHIP_SUCCESS,
            RELATIONSHIP_UNMATCHED).collect(toSet());

}
