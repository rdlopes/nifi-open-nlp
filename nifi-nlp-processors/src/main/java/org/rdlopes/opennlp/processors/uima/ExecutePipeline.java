package org.rdlopes.opennlp.processors.uima;

import lombok.Getter;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.services.opennlp.UIMA;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ExecutePipeline extends BaseProcessor {

    public static final PropertyDescriptor UIMA_SERVICE = new PropertyDescriptor
            .Builder().name("UIMA_SERVICE")
            .displayName("UIMA Service")
            .description("The UIMA service that will execute the pipeline")
            .identifiesControllerService(UIMA.class)
            .required(true)
            .build();

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
            Stream.of(UIMA_SERVICE))
            .collect(toList());

    @Override
    protected void processInput(ProcessContext processContext, InputStream in, Map<String, String> attributes) {
        UIMA uima = processContext.getProperty(UIMA_SERVICE)
                .asControllerService(UIMA.class);
        uima.execute(in, attributes);
    }

}
