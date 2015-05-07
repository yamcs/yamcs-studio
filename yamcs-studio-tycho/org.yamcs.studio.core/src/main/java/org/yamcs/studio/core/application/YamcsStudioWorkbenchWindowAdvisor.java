package org.yamcs.studio.core.application;

import org.csstudio.ui.menu.app.ApplicationActionBarAdvisor;
import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.yamcs.studio.core.YamcsPlugin;

public class YamcsStudioWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor {

    public YamcsStudioWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void preWindowOpen() {
        super.preWindowOpen();
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1920, 1200));
        configurer.setShowPerspectiveBar(false);
        configurer.setShowStatusLine(false); // Shouldn't do anything. Controlled through e4xmi
        setTitle(configurer, "Yamcs Studio");
    }

    @Override
    public void postWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        String perspectiveLabel = configurer.getWindow().getActivePage().getPerspective().getLabel();
        setTitle(configurer, perspectiveLabel);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    private void setTitle(IWorkbenchWindowConfigurer configurer, String label) {
        String host = YamcsPlugin.getDefault().getHost();
        String instance = YamcsPlugin.getDefault().getInstance();
        configurer.setTitle(String.format("%s \u2022 anonymous@%s/%s", label, host, instance));
    }
}
