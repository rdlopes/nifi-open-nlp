package org.rdlopes.processors.opennlp;

import org.apache.nifi.processor.Processor;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.*;

public abstract class AbstractNlpProcessorTest {
    private final boolean ensureTrainingDataPresence;

    private final Class<? extends Processor> processClass;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    TestRunner testRunner;

    AbstractNlpProcessorTest(Class<? extends Processor> processClass, boolean ensureTrainingDataPresence) {
        this.processClass = processClass;
        this.ensureTrainingDataPresence = ensureTrainingDataPresence;
    }

    @Before
    public void init() throws IOException {
        testRunner = TestRunners.newTestRunner(processClass);
        testRunner.setProperty(PROPERTY_MODEL_STORE_PATH, testFolder.newFolder("model-store").getAbsolutePath());
    }

    @Test
    public void shouldBeInvalidBecauseInsufficientContentData() {
        testRunner.setProperty(PROPERTY_TRAINING_DATA, "\n");
        testRunner.assertNotValid();
    }

    @Test
    public void shouldBeInvalidBecauseInsufficientFileData() {
        testRunner.setProperty(PROPERTY_TRAINING_FILE_PATH, getClass().getResource("/training/empty.train").getFile());
        testRunner.assertNotValid();
    }

    @Test
    public void shouldBeInvalidBecauseNoTrainingData() {
        if (ensureTrainingDataPresence) {
            testRunner.assertNotValid();
        }
    }

    @After
    public void tearDown() throws Exception {
        testRunner.shutdown();
    }

}
