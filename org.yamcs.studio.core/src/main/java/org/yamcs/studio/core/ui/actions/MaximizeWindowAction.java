package org.yamcs.studio.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class MaximizeWindowAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public MaximizeWindowAction(IWorkbenchWindow window) {
        super("Maximize");
        this.window = window;
    }

    @Override
    public void run() {
        window.getShell().setMaximized(true);
    }

    @Override
    public void dispose() {
        window = null;
    }
}
