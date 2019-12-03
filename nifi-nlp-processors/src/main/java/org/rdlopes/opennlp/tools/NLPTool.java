package org.rdlopes.opennlp.tools;

import lombok.Getter;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.exception.ProcessException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.apache.nifi.util.StringUtils.isBlank;

public abstract class NLPTool<M extends BaseModel> {

    @Getter
    private final ComponentLog logger;

    @Getter
    private final Class<M> modelClass;

    @Getter
    private final Path modelPath;

    NLPTool(Class<M> modelClass, Path modelPath, ComponentLog logger) {
        this.modelClass = modelClass;
        this.modelPath = modelPath;
        this.logger = logger;
    }

    public void createModelFromPreTrained(Path preTrainedPath) throws IOException {
        getLogger().debug("createModelFromPreTrained | source:{} | target:{}", new Object[]{preTrainedPath, modelPath});
        ensureModelDirectoryExists();

        if (modelExists()) {
            copy(preTrainedPath, modelPath, REPLACE_EXISTING);
        } else {
            copy(preTrainedPath, modelPath);
        }
    }

    public void createModelFromTraining(ValidationContext validationContext,
                                        TrainingParameters trainingParameters,
                                        String trainingLanguage,
                                        String trainingFilePath,
                                        String trainingData)
            throws IOException {
        M model = null;

        if (trainingFilePath != null) {
            File dataFile = get(trainingFilePath).normalize().toFile();
            model = trainModel(validationContext, new MarkableFileInputStreamFactory(dataFile), trainingParameters, trainingLanguage);
        }

        if (!isBlank(trainingData)) {
            model = trainModel(validationContext, () -> toInputStream(trainingData, UTF_8), trainingParameters, trainingLanguage);
        }

        if (model != null) {
            getLogger().info("Storing model {} at {}", new Object[]{model, modelPath});
            ensureModelDirectoryExists();
            deleteIfExists(modelPath);
            model.serialize(modelPath);
        }
    }

    private void ensureModelDirectoryExists() throws IOException {
        if (!modelPath.getParent().toFile().exists()) {
            createDirectory(modelPath.getParent());
        }
    }

    protected abstract void evaluate(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes, M model, Map<String, String> evaluation) throws IOException;

    public boolean modelExists() {
        return modelPath.toFile().exists();
    }

    public Map<String, String> processContent(ProcessContext processContext, InputStream content, Charset charset, Map<String, String> attributes) {
        Map<String, String> evaluation = new HashMap<>();
        try {
            final M model = modelClass.getConstructor(File.class)
                                      .newInstance(modelPath.toFile());
            evaluate(processContext, content, charset, attributes, model, evaluation);
        } catch (Exception e) {
            throw new ProcessException("Loading model from " + modelPath + " failed", e);
        }
        return evaluation;
    }

    public void removeModel() {
        try {
            deleteIfExists(modelPath);
        } catch (IOException e) {
            throw new ProcessException("Deleting model from " + modelPath + " failed", e);
        }
    }

    protected abstract M trainModel(ValidationContext validationContext,
                                    InputStreamFactory inputStreamFactory,
                                    TrainingParameters trainingParameters,
                                    String trainingLanguage) throws IOException;
}
