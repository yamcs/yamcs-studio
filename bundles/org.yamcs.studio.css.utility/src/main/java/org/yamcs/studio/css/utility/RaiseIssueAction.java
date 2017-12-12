package org.yamcs.studio.css.utility;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class RaiseIssueAction extends Action implements IWorkbenchAction {

    public static final String ID = "org.yamcs.studio.ui.actions.raiseIssue";

    public RaiseIssueAction() {
        super("Raise an Issue");
        setId(ID);
    }

    @Override
    public String getToolTipText() {
        return "Raise an Issue on GitHub";
    }

    @Override
    public void run() {
        Program.launch("https://github.com/yamcs/yamcs-studio/issues/");
    }

    @Override
    public void dispose() {
    }
}
