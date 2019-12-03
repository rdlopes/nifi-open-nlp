package org.rdlopes.opennlp.common;

import lombok.Getter;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.rdlopes.opennlp.common.NLPAttribute.NLP_EVALUATION_ERROR_KEY;

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

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession processSession) throws ProcessException {
        FlowFile flowFile = Optional.ofNullable(processSession.get())
                .orElseGet(processSession::create);

        final ConcurrentMap<String, String> attributes = new ConcurrentHashMap<>(flowFile.getAttributes());
        Relationship relationship = RELATIONSHIP_UNMATCHED;

        try {
            processSession.read(flowFile, in -> processInput(processContext, in, attributes));
            flowFile = processSession.putAllAttributes(flowFile, attributes);
            relationship = RELATIONSHIP_SUCCESS;
            getLogger().debug("onTrigger | flow file content evaluated: {}", new Object[]{attributes});

        } catch (ProcessException e) {
            flowFile = NLPAttribute.set(NLP_EVALUATION_ERROR_KEY, processSession, flowFile, e.getMessage());
            relationship = RELATIONSHIP_UNMATCHED;
            getLogger().warn("Error while evaluating content", e);

        } finally {
            processSession.getProvenanceReporter().route(flowFile, relationship);
            processSession.transfer(flowFile, relationship);
            getLogger().info("Routing {} to {}", new Object[]{flowFile, relationship});
        }

    }

    protected abstract void processInput(ProcessContext processContext, InputStream in, Map<String, String> attributes);

}
