package org.yamcs.studio.core.application;

import org.csstudio.ui.menu.app.ApplicationActionBarAdvisor;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.application.IActionBarConfigurer;

public class YamcsStudioActionBarAdvisor extends ApplicationActionBarAdvisor {

    public YamcsStudioActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolbar) {
        // TODO Auto-generated method stub
        super.fillCoolBar(coolbar);
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        super.fillStatusLine(statusLine);
    }
}
