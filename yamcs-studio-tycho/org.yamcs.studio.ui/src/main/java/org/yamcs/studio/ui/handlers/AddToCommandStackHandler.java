package org.yamcs.studio.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.ui.commanding.stack.AddToStackWizard;
import org.yamcs.studio.ui.commanding.stack.CommandStackView;

public class AddToCommandStackHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(CommandStackView.ID);
        CommandStackView commandStackView = (CommandStackView) part;

        AddToStackWizard wizard = new AddToStackWizard();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        if (dialog.open() == Window.OK) {
            commandStackView.addTelecommand(wizard.getTelecommand());
        }
        return null;
    }
}
