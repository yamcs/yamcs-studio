package org.yamcs.studio.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class MinimizeWindowAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public MinimizeWindowAction(IWorkbenchWindow window) {
        super("Minimize");
        this.window = window;
    }

    @Override
    public void run() {
        window.getShell().setMinimized(true);
    }

    @Override
    public void dispose() {
        window = null;
    }
}
