package org.yamcs.studio.core.application;

import org.csstudio.ui.menu.app.ApplicationActionBarAdvisor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

@SuppressWarnings("restriction")
public class YamcsStudioActionBarAdvisor extends ApplicationActionBarAdvisor {

    public YamcsStudioActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);

        // Redefined in our own plugin.xml to customize it
        removeActionById("org.csstudio.opibuilder.actionSet");
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolbar) {
        super.fillCoolBar(coolbar);
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        super.fillStatusLine(statusLine);
    }

    private void removeActionById(String actionSetId) {
        // Use of an internal API is required to remove actions that are provided
        // by including Eclipse bundles.
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
        IActionSetDescriptor[] actionSets = reg.getActionSets();
        for (int i = 0; i < actionSets.length; i++) {
            if (actionSets[i].getId().equals(actionSetId)) {
                IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
                reg.removeExtension(ext, new Object[] { actionSets[i] });
                return;
            }
        }
    }
}
