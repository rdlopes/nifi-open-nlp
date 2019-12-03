package org.rdlopes.opennlp.processors.uima;

import org.apache.nifi.util.MockFlowFile;
import org.junit.Test;
import org.rdlopes.opennlp.common.BaseProcessorTest;
import org.rdlopes.opennlp.services.uima.UIMAService;

import static org.rdlopes.opennlp.common.BaseProcessor.RELATIONSHIP_SUCCESS;
import static org.rdlopes.opennlp.common.BaseProcessor.RELATIONSHIP_UNMATCHED;

public class ExecutePipelineTest extends BaseProcessorTest<ExecutePipeline> {

    private UIMAService uima;

    public ExecutePipelineTest() {
        super(ExecutePipeline.class);
    }

    @Override
    public void init() throws Exception {
        super.init();
        uima = new UIMAService();
        testRunner.addControllerService("uima-service", uima);
        testRunner.enableControllerService(uima);
        testRunner.setProperty(ExecutePipeline.UIMA_SERVICE, "uima-service");
    }

    @Test
    public void shouldTokenize() {
        testRunner.assertValid();
        testRunner.enqueue("");
        testRunner.run();
        testRunner.assertTransferCount(RELATIONSHIP_UNMATCHED, 0);
        testRunner.assertTransferCount(RELATIONSHIP_SUCCESS, 1);

        MockFlowFile flowFile = testRunner.getFlowFilesForRelationship(RELATIONSHIP_SUCCESS).iterator().next();
    }

}
