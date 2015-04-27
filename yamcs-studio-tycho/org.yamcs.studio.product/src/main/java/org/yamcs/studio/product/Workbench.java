package org.yamcs.studio.product;

import java.util.Map;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class Workbench extends org.csstudio.utility.product.Workbench {

    @Override
    protected WorkbenchAdvisor createWorkbenchAdvisor(final Map<String, Object> parameters) {
        OpenDocumentEventProcessor openDocProcessor =
                (OpenDocumentEventProcessor) parameters.get(OpenDocumentEventProcessor.OPEN_DOC_PROCESSOR);
        return new YamcsStudioWorkbenchAdvisor(openDocProcessor);
    }
}
