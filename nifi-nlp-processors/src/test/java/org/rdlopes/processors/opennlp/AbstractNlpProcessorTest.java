package org.rdlopes.processors.opennlp;

import org.apache.nifi.processor.Processor;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.PROPERTY_TRAINING_DATA;
import static org.rdlopes.processors.opennlp.AbstractNlpProcessor.PROPERTY_TRAINING_FILE_PATH;

public abstract class AbstractNlpProcessorTest {
    private final boolean ensureTrainingDataPresence;

    private final Class<? extends Processor> processClass;

    TestRunner testRunner;

    AbstractNlpProcessorTest(Class<? extends Processor> processClass, boolean ensureTrainingDataPresence) {
        this.processClass = processClass;
        this.ensureTrainingDataPresence = ensureTrainingDataPresence;
    }

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(processClass);
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
    public void tearDown() {
        testRunner.shutdown();
    }

}
