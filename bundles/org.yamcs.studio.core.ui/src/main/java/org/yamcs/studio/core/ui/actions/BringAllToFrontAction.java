package org.yamcs.studio.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class BringAllToFrontAction extends Action implements IWorkbenchAction {

    private IWorkbench workbench;

    public BringAllToFrontAction(IWorkbench workbench) {
        super("Bring All to Front");
        this.workbench = workbench;
    }

    @Override
    public void run() {
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            window.getShell().setActive();
        }
    }

    @Override
    public void dispose() {
        workbench = null;
    }
}
