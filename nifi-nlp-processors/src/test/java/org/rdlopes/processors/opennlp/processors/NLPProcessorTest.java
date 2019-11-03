package org.rdlopes.processors.opennlp.processors;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;

import static org.rdlopes.processors.opennlp.common.NLPProperty.TRAINED_MODEL_FILE_PATH;

public abstract class NLPProcessorTest<P extends NLPProcessor<?, ?>> {
    private final Class<P> processorClass;

    protected TestRunner testRunner;

    protected NLPProcessorTest(Class<P> processorClass) {this.processorClass = processorClass;}

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(processorClass);
        setupVariables();
    }

    protected void setModelFilePath(String filePath) {
        testRunner.setProperty(TRAINED_MODEL_FILE_PATH.descriptor, getClass().getResource(filePath).getFile());
    }

    private void setupVariables() {
        testRunner.setVariable("NIFI_HOME", "../nifi-local-data");
        testRunner.setVariable("nlp.models.storage.directory", "target/test-classes/store");
    }

}
