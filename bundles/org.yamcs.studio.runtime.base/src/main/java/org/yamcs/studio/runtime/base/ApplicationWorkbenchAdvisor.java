package org.yamcs.studio.runtime.base;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return "org.yamcs.studio.runtime.perspective";
    }

    @Override
    public void postStartup() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        YamcsUIPlugin.getDefault().postWorkbenchStartup(workbench);
        System.out.println("got.. " + Platform.getInstanceLocation());
    }
}
