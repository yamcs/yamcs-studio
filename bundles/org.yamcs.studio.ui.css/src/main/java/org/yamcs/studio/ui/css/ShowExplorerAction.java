package org.yamcs.studio.ui.css;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class ShowExplorerAction implements IObjectActionDelegate {

    private IWorkbenchWindow window;

    @Override
    public void run(IAction action) {
        try {
            if (window == null) {
                window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            }
            IWorkbenchPage page = window.getActivePage();
            page.showView("org.yamcs.studio.explorer.view");
        } catch (WorkbenchException e) {
            String message = "Failed to open Explorer. \n" + e.getMessage();
            MessageDialog.openError(null, "Error", message);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        window = targetPart.getSite().getWorkbenchWindow();
    }

}
