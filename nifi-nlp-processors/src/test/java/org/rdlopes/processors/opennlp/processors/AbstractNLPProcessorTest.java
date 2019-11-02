package org.rdlopes.processors.opennlp.processors;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;

public abstract class AbstractNLPProcessorTest<P extends AbstractNLPProcessor<?>> {
    protected static String SAMPLE_CONTENT_VINKEN = "Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n" +
                                                    "Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group .\n" +
                                                    "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named\n" +
                                                    "    a director of this British industrial conglomerate .";

    protected static String[] SAMPLE_TAGS_VINKEN = {"NNP", "NNP", ",", "CD", "NNS", "JJ", ",", "MD", "VB", "DT", "NN", "IN", "DT", "JJ", "NN", "NNP", ".", "CD", ".", "NNP", ".", "NNP", "VBZ",
                                                    "NN", "IN", "NNP", "NNP", ".", "NNP", ".", ",", "DT", "JJ", "NN", "NN", ".", "NNP", "NNP", ",", "CD", "NNS", "JJ", "CC", "JJ", "NN", "IN",
                                                    "NNP", "NNP", "NNP", "NNP", ",", "VBD", "VBN", "DT", "NN", "IN", "DT", "JJ", "JJ", "NN", "."};

    protected static String[] SAMPLE_TOKENS_VINKEN = {"Pierre", "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "nonexecutive", "director",
                                                      "Nov", ".", "29", ".", "Mr", ".", "Vinken", "is", "chairman", "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch",
                                                      "publishing", "group", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former", "chairman", "of", "Consolidated",
                                                      "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", "."};

    private final Class<P> processorClass;

    protected TestRunner testRunner;

    protected AbstractNLPProcessorTest(Class<P> processorClass) {this.processorClass = processorClass;}

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(processorClass);
        setupVariables();
    }

    protected void setupVariables() {
        testRunner.setVariable("NIFI_HOME", "../nifi-local-data");
        testRunner.setVariable("nlp.models.storage.directory", "target/test-classes");
    }

}
