package org.yamcs.studio.product;

import org.csstudio.ui.menu.app.ApplicationActionBarAdvisor;
import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

public class YamcsStudioWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor {

    public YamcsStudioWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void preWindowOpen() {
        super.preWindowOpen();
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1920, 1200));
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
}
