package org.rdlopes.opennlp.common;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class BaseProcessorTest<P extends BaseProcessor> {
    private final Class<P> processorClass;

    protected TestRunner testRunner;

    public BaseProcessorTest(Class<P> processorClass) {
        this.processorClass = processorClass;
    }

    @Before
    public void init() throws Exception {
        testRunner = TestRunners.newTestRunner(processorClass);
        testRunner.setVariable("NIFI_HOME", "../nifi-local-data");
        testRunner.setVariable("nlp.models.storage.directory", "target/test-classes/model-store");
    }

    protected Path getFilePath(String localPath) throws URISyntaxException {
        URI uri = getClass().getResource(localPath).toURI();
        String mainPath = Paths.get(uri).normalize().toString();
        return Paths.get(mainPath);
    }

    protected P getProcessor() {
        return Optional.ofNullable(testRunner.getProcessor())
                .map(processorClass::cast)
                .orElse(null);
    }

}
