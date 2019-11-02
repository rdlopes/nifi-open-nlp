package org.rdlopes.processors.opennlp.tools;

import lombok.Getter;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;

import java.nio.file.Path;
import java.util.Map;

public abstract class NLPTool<M extends BaseModel> {

    @Getter
    private final Class<M> modelClass;

    protected NLPTool(Class<M> modelClass) {this.modelClass = modelClass;}

    public abstract void copyPreTrainedModel(String identifier, Path sourceModelPath);

    public abstract M createModelFromTrainingData(String identifier, ValidationContext validationContext, String trainingLanguage, TrainingParameters trainingParameters,
                                                  InputStreamFactory inputStreamFactory);

    public abstract void deleteModel(String identifier);

    public abstract Map<String, String> evaluateFlowFileContent(String identifier, ProcessContext context, String content, FlowFile flowFile);
}
