package org.yamcs.studio.runtime.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class BringAllToFrontAction extends Action implements IWorkbenchAction {

    public static final String ID = "org.yamcs.studio.runtime.actions.bringAllToFront";

    public BringAllToFrontAction() {
        super("Bring All to Front");
        setId(ID);
    }

    @Override
    public void run() {
        Program.launch("https://github.com/yamcs/yamcs-studio/issues/");
    }

    @Override
    public void dispose() {
    }
}
