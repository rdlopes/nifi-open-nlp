package org.rdlopes.opennlp.services.uima;

import lombok.extern.slf4j.Slf4j;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.exception.ProcessException;
import org.rdlopes.services.opennlp.UIMA;

@Slf4j
public class UIMAService extends AbstractControllerService implements UIMA {

    @Override
    public void execute() throws ProcessException {
        log.debug("executing pipeline");
    }

}
