package org.yamcs.studio.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class OnlineHelpAction extends Action implements IWorkbenchAction {

    public static final String ID = "org.yamcs.studio.core.ui.actions.onlineHelp";

    public OnlineHelpAction() {
        super("Yamcs Studio Help");
        setId(ID);
    }

    @Override
    public String getToolTipText() {
        return "Open Online Documentation";
    }

    @Override
    public void run() {
        Program.launch("https://www.yamcs.org/docs/");
    }

    @Override
    public void dispose() {
    }
}
