package org.yamcs.studio.core.application;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.csstudio.utility.product.ApplicationWorkbenchAdvisor;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.PlatformUI;
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
        return IDs.OPI_RUNTIME_PERSPECTIVE;
    }

    @Override
    public void postStartup() {
        PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
        pm.remove("org.eclipse.help.ui.browsersPreferencePage");
        pm.remove("org.eclipse.team.ui.TeamPreferences");
        pm.remove("org.csstudio.platform.ui.css.applications");
        pm.remove("org.csstudio.platform.ui.css.platform");
    }
}
