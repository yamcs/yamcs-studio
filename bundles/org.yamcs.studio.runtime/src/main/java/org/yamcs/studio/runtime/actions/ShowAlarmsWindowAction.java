package org.yamcs.studio.runtime.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ShowAlarmsWindowAction extends Action implements IWorkbenchAction {

    public static final String ID = "org.yamcs.studio.runtime.actions.showAlarmsWindow";

    public ShowAlarmsWindowAction() {
        super("Bring All to Front");
        setId(ID);
    }

    @Override
    public void run() {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        // activeWindow.openPage(perspectiveId, input)
    }

    @Override
    public void dispose() {
    }
}
