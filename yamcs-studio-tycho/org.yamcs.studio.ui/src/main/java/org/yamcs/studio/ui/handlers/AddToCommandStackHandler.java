package org.yamcs.studio.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.ui.commanding.stack.AddToStackWizard;

public class AddToCommandStackHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        WizardDialog dialog = new WizardDialog(window.getShell(), new AddToStackWizard());
        dialog.open();
        return null;
    }
}
