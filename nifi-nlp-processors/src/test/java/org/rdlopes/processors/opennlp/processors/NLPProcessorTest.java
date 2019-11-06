package org.rdlopes.processors.opennlp.processors;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;

public abstract class NLPProcessorTest<P extends NLPProcessor<?, ?>> {
    private final Class<P> processorClass;

    protected TestRunner testRunner;

    protected NLPProcessorTest(Class<P> processorClass) {this.processorClass = processorClass;}

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(processorClass);
        setupVariables();
    }

    private void setupVariables() {
        testRunner.setVariable("NIFI_HOME", "../nifi-local-data");
        testRunner.setVariable("nlp.models.storage.directory", "target/test-classes/store");
    }

}
