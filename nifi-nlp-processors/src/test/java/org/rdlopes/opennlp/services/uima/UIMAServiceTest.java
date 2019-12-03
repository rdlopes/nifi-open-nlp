package org.rdlopes.opennlp.services.uima;

import lombok.Getter;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.reporting.InitializationException;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.opennlp.common.BaseProcessorTest;
import org.rdlopes.services.opennlp.UIMA;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.rdlopes.opennlp.services.uima.UIMAServiceTest.TestingProcessor;

public class UIMAServiceTest extends BaseProcessorTest<TestingProcessor> {

    private UIMAService uima;

    public UIMAServiceTest() {
        super(TestingProcessor.class);
    }

    @Test
    public void shouldExecutePipeline() throws InitializationException {
        prepareTest(uima -> uima.execute(toInputStream("", UTF_8), emptyMap()));
    }

    public void prepareTest(Consumer<UIMA> serviceConsumer) throws InitializationException {
        uima = new UIMAService();
        testRunner.addControllerService("uima-service", uima);
        testRunner.enableControllerService(uima);
        testRunner.setProperty(TestingProcessor.UIMA_SERVICE, "uima-service");
        testRunner.assertValid();
        getProcessor().serviceConsumer = serviceConsumer;

        testRunner.enqueue("");
        testRunner.run();
        testRunner.assertQueueEmpty();
    }

    public static class TestingProcessor extends BaseProcessor {
        private Consumer<UIMA> serviceConsumer;

        public static final PropertyDescriptor UIMA_SERVICE = new PropertyDescriptor
                .Builder().name("UIMA_SERVICE")
                .displayName("UIMA Service")
                .description("The UIMA service that will execute the pipeline")
                .identifiesControllerService(UIMA.class)
                .required(true)
                .build();
        @Getter
        private final List<PropertyDescriptor> supportedPropertyDescriptors = Stream.concat(super.getSupportedPropertyDescriptors().stream(),
                Stream.of(TestingProcessor.UIMA_SERVICE))
                .collect(toList());

        @Override
        protected void processInput(ProcessContext processContext, InputStream in, Map<String, String> attributes) {
            UIMA uima = processContext.getProperty(UIMA_SERVICE).asControllerService(UIMA.class);
            this.serviceConsumer.accept(uima);
        }

    }


}
