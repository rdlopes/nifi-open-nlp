package org.rdlopes.opennlp.processors.uima;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.services.opennlp.UIMAPipeline;

public class ExecutePipeline extends BaseProcessor {

    public static final PropertyDescriptor UIMA_SERVICE = new PropertyDescriptor
            .Builder().name("UIMA_SERVICE")
            .displayName("UIMA Service")
            .description("The UIMA service that will execute the pipeline")
            .identifiesControllerService(UIMAPipeline.class)
            .required(true)
            .build();

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession processSession) throws ProcessException {
        UIMAPipeline uima = processContext.getProperty(UIMA_SERVICE).asControllerService(UIMAPipeline.class);
        uima.execute();
    }

}
