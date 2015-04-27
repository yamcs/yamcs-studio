package org.yamcs.studio.product;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.csstudio.utility.product.ApplicationWorkbenchAdvisor;
import org.csstudio.utility.product.CSStudioPerspective;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class YamcsStudioWorkbenchAdvisor extends ApplicationWorkbenchAdvisor {

    public YamcsStudioWorkbenchAdvisor(OpenDocumentEventProcessor openDocProcessor) {
        super(openDocProcessor);
    }

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new YamcsStudioWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return CSStudioPerspective.ID;
    }
}
