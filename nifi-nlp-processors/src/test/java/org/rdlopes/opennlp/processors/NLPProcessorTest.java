package org.rdlopes.opennlp.processors;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class NLPProcessorTest<P extends NLPProcessor<?, ?>> {
    private final Class<P> processorClass;

    protected TestRunner testRunner;

    protected NLPProcessorTest(Class<P> processorClass) {this.processorClass = processorClass;}

    protected Path getFilePath(String localPath) throws URISyntaxException {
        URI uri = getClass().getResource(localPath).toURI();
        String mainPath = Paths.get(uri).normalize().toString();
        return Paths.get(mainPath);
    }

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
