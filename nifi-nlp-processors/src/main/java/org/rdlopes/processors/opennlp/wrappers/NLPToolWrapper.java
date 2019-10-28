package org.rdlopes.processors.opennlp.wrappers;

import lombok.Getter;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.exception.ProcessException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

public abstract class NLPToolWrapper<T, M extends BaseModel> {

    @Getter
    private final Class<M> modelClass;

    protected NLPToolWrapper(Class<M> modelClass) {this.modelClass = modelClass;}

    public abstract void evaluateContent(ProcessContext context, M model, String content, Map<String, String> attributes);

    public M loadModel(Path modelFile) {
        M result;
        try {
            result = modelClass.getConstructor(File.class)
                               .newInstance(modelFile.toFile());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ProcessException("Loading model from " + modelFile + " failed", e);
        }
        return result;
    }

    public abstract M trainModel(PropertyContext propertyContext,
                                 String trainingLanguage,
                                 Charset charset,
                                 TrainingParameters trainingParameters,
                                 InputStreamFactory inputStreamFactory) throws IOException;
}
