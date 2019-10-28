package org.rdlopes.processors.opennlp.processors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.util.model.BaseModel;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.rdlopes.processors.opennlp.wrappers.NLPToolWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toSet;
import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_REQUIRED;
import static org.rdlopes.processors.opennlp.common.NLPAttribute.COMMON_ERROR;
import static org.rdlopes.processors.opennlp.common.NLPProperty.COMMON_CHARACTER_SET;
import static org.rdlopes.processors.opennlp.common.NLPProperty.COMMON_MODELS_STORAGE_DIRECTORY;

@EventDriven
@SupportsBatching
@InputRequirement(INPUT_REQUIRED)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractNLPProcessor<T, M extends BaseModel> extends AbstractProcessor {

    public static final Relationship RELATIONSHIP_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Parsing completed successfully")
            .build();

    public static final Relationship RELATIONSHIP_UNMATCHED = new Relationship.Builder()
            .name("unmatched")
            .description("Unmatched content")
            .build();

    @Getter
    private final Set<Relationship> relationships = Stream.of(RELATIONSHIP_SUCCESS, RELATIONSHIP_UNMATCHED)
                                                          .collect(toSet());

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors =
            Arrays.asList(COMMON_CHARACTER_SET.descriptor, COMMON_MODELS_STORAGE_DIRECTORY.descriptor);

    @Getter
    private final NLPToolWrapper<T, M> wrapper;

    protected AbstractNLPProcessor(NLPToolWrapper<T, M> wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        Collection<ValidationResult> results = new ArrayList<>(super.customValidate(validationContext));

        final Charset charset = Charset.forName(COMMON_CHARACTER_SET.getStringFrom(validationContext));
        String modelsStorageDirectory = COMMON_MODELS_STORAGE_DIRECTORY.getStringFrom(validationContext);
        getLogger().debug("customValidate |  models storage:{} | charset:{}", new Object[]{modelsStorageDirectory, charset});

        try {
            validateModel(validationContext, results, charset, modelsStorageDirectory);

        } catch (Exception e) {
            getLogger().warn("model validation error", e);
            results.add(new ValidationResult.Builder()
                                .valid(false)
                                .input(modelsStorageDirectory)
                                .subject("model")
                                .explanation(e.getMessage())
                                .build());
        }
        return results;
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) {
        Optional.ofNullable(session.get())
                .ifPresent(flowFile -> {

                    final Charset charset = Charset.forName(COMMON_CHARACTER_SET.getStringFrom(context));
                    String modelsStorageDirectory = COMMON_MODELS_STORAGE_DIRECTORY.getStringFrom(context);
                    getLogger().debug("onTrigger | charset:{} | models storage:{}", new Object[]{charset, modelsStorageDirectory});

                    AtomicReference<String> flowFileContent = new AtomicReference<>();
                    session.read(flowFile, in -> flowFileContent.set(IOUtils.toString(in, charset)));
                    Relationship relationship = RELATIONSHIP_UNMATCHED;

                    try {
                        final Path storagePath = get(modelsStorageDirectory);
                        final Path filePath = storagePath.resolve(getIdentifier() + ".bin");

                        M model = wrapper.loadModel(filePath);
                        Map<String, String> attributes = new HashMap<>(flowFile.getAttributes());
                        wrapper.evaluateContent(context, model, flowFileContent.get(), attributes);

                        flowFile = session.putAllAttributes(flowFile, attributes);
                        relationship = RELATIONSHIP_SUCCESS;
                        getLogger().debug("onTrigger | flow file content evaluated: {}", new Object[]{attributes});

                    } catch (Exception e) {
                        getLogger().warn("Error while evaluating content", e);
                        flowFile = COMMON_ERROR.updateFlowFile(session, flowFile, e.getMessage());
                        relationship = RELATIONSHIP_UNMATCHED;

                    } finally {
                        session.getProvenanceReporter().route(flowFile, relationship);
                        session.transfer(flowFile, relationship);
                        getLogger().info("Routing {} to {}", new Object[]{flowFile, relationship});
                    }
                });
    }

    protected abstract void validateModel(ValidationContext validationContext, Collection<ValidationResult> results, Charset charset, String modelsStorageDirectory) throws IOException;

}
