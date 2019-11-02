package org.rdlopes.processors.opennlp.processors.trained;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import opennlp.tools.util.model.BaseModel;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.rdlopes.processors.opennlp.processors.AbstractNLPProcessor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractPreTrainedProcessor<M extends BaseModel> extends AbstractNLPProcessor<M> {

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                                                                                        Stream.of(TRAINED_MODEL_FILE_PATH.descriptor))
                                                                                .collect(toList());

    @Override
    protected void validateModel(ValidationContext validationContext, Collection<ValidationResult> results, Charset charset, String modelsStorageDirectory) throws IOException {
        final Path sourcePath = get(TRAINED_MODEL_FILE_PATH.getStringFrom(validationContext));
        final Path storagePath = get(modelsStorageDirectory).resolve(getIdentifier() + ".bin");
        getLogger().debug("validateModel | sourcePath:{} | storagePath:{}", new Object[]{sourcePath, storagePath});

        if (!sourcePath.toFile().exists()) {
            results.add(new ValidationResult.Builder()
                                .input(sourcePath.toString()).valid(false)
                                .subject("Model source path")
                                .explanation("does not exist").build());
        }

        if (!storagePath.getParent().toFile().exists()) {
            createDirectory(storagePath.getParent());
        }
        deleteIfExists(storagePath);
        copy(sourcePath, storagePath);
    }
}
