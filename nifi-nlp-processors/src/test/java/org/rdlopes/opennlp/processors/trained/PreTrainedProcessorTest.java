package org.rdlopes.opennlp.processors.trained;

import org.rdlopes.opennlp.common.NLPProperty;
import org.rdlopes.opennlp.processors.NLPProcessor;
import org.rdlopes.opennlp.processors.NLPProcessorTest;

public abstract class PreTrainedProcessorTest<P extends NLPProcessor<?, ?>> extends NLPProcessorTest<P> {
    protected static final String SAMPLE_CONTENT =
            "== Please notice that this announcement will be updated at 10:30 AM, 3:00 PM and 7:00 PM ==\n" +
            "\n" +
            "Pierre Vinken, 61 years old, will join the board as a non-executive director Nov. 29th.\n" +
            "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group that owns 40% of published magazines in the Netherlands and 10% in Belgium.\n" +
            "Elsevier N.V. now represents 51% of the total capital of the company, worth more than $800.000 (1.000.000 euros, 900.000 pounds).\n" +
            "Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a director of this British industrial conglomerate.";

    protected static final String[] SAMPLE_SENTENCES = new String[]{
            "== Please notice that this announcement will be updated at 10:30 AM, 3:00 PM and 7:00 PM ==\n\n" +
            "Pierre Vinken, 61 years old, will join the board as a non-executive director Nov. 29th.",
            "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group that owns 40% of published magazines in the Netherlands and " +
            "10% in Belgium.",
            "Elsevier N.V. now represents 51% of the total capital of the company, worth more than $800.000",
            "(1.000.000",
            "euros, 900.000 pounds).",
            "Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a director of this British industrial" +
            " conglomerate."};

    protected static final String[] SAMPLE_TAGS_SIMPLE = new String[]{
            "NN", "VB", "NN", "IN", "DT", "NN", "MD", "VB", "VBN", "IN", "CD", ":", "CD", "VBP", ",", "CD", ":", "CD", "NNP", "CC", "CD", ":", "CD", "NNP", "NNP", "NNP", "NNP", ",", "CD", "NNS",
            "JJ", ",", "MD", "VB", "DT", "NN", "IN", "DT", "FW", ":", "NN", "NN", "NNP", ".", "CD", "NN", ".", "NNP", ".", "NNP", "VBZ", "NN", "IN", "NNP", "NNP", ".", "NNP", ".", ",", "DT", "JJ",
            "NN", "NN", "WDT", "VBZ", "CD", "NN", "IN", "VBN", "NNS", "IN", "DT", "NNP", "CC", "CD", "NN", "IN", "NNP", ".", "NNP", "NNP", ".", "NNP", ".", "RB", "VBZ", "CD", "NN", "IN", "DT", "JJ",
            "NN", "IN", "DT", "NN", ",", "NN", "JJR", "IN", "$", "CD", ".", "IN", "-LRB-", "LS", ".", "CD", ".", "CD", "NNS", ",", "CD", ".", "CD", "NNS", "-RRB-", ".", "NNP", "NNP", ",", "CD",
            "NNS", "JJ", "CC", "JJ", "NN", "IN", "NNP", "NNP", "NNP", "NNP", ",", "VBD", "VBN", "DT", "NN", "IN", "DT", "JJ", "JJ", "NN", "."};

    protected static final String[] SAMPLE_TOKENS_SIMPLE = new String[]{
            "==", "Please", "notice", "that", "this", "announcement", "will", "be", "updated", "at", "10", ":", "30", "AM", ",", "3", ":", "00", "PM", "and", "7", ":", "00", "PM", "==", "Pierre",
            "Vinken", ",", "61", "years", "old", ",", "will", "join", "the", "board", "as", "a", "non", "-", "executive", "director", "Nov", ".", "29", "th", ".", "Mr", ".", "Vinken", "is",
            "chairman", "of", "Elsevier", "N", ".", "V", ".", ",", "the", "Dutch", "publishing", "group", "that", "owns", "40", "%", "of", "published", "magazines", "in", "the", "Netherlands",
            "and", "10", "%", "in", "Belgium", ".", "Elsevier", "N", ".", "V", ".", "now", "represents", "51", "%", "of", "the", "total", "capital", "of", "the", "company", ",", "worth", "more",
            "than", "$", "800", ".", "000", "(", "1", ".", "000", ".", "000", "euros", ",", "900", ".", "000", "pounds", ")", ".", "Rudolph", "Agnew", ",", "55", "years", "old", "and", "former",
            "chairman", "of", "Consolidated", "Gold", "Fields", "PLC", ",", "was", "named", "a", "director", "of", "this", "British", "industrial", "conglomerate", "."};

    private final String modelFilePath;
    private final boolean requiresModel;

    protected PreTrainedProcessorTest(Class<P> processorClass, String modelFilePath, boolean requiresModel) {
        super(processorClass);
        this.modelFilePath = modelFilePath;
        this.requiresModel = requiresModel;
    }

    protected PreTrainedProcessorTest(Class<P> processorClass, String modelFilePath) {
        this(processorClass, modelFilePath, true);
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (requiresModel) {
            testRunner.setProperty(NLPProperty.TRAINED_MODEL_FILE_PATH.descriptor, getFilePath(modelFilePath).toString());
        }
    }

}
