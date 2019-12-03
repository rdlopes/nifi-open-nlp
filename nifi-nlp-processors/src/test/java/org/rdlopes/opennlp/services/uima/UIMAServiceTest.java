package org.rdlopes.opennlp.services.uima;

import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessor;
import org.rdlopes.services.opennlp.UIMA;

import java.util.function.Consumer;

import static org.rdlopes.opennlp.processors.uima.ExecutePipeline.UIMA_SERVICE;

public class UIMAServiceTest {

    private TestRunner testRunner;

    private TestingProcessor testingProcessor;

    @Before
    public void init() {
        testingProcessor = new TestingProcessor();
        testRunner = TestRunners.newTestRunner(testingProcessor);
    }

    @Test
    public void shouldExecutePipeline() {
        testingProcessor.prepareTest(UIMA::execute);
    }

    private static class TestingProcessor extends BaseProcessor {
        private Consumer<UIMA> serviceConsumer;

        @Override
        public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
            UIMA uima = context.getProperty(UIMA_SERVICE).asControllerService(UIMA.class);
            this.serviceConsumer.accept(uima);
        }

        public void prepareTest(Consumer<UIMA> serviceConsumer) {
            this.serviceConsumer = serviceConsumer;
        }
    }


}
