package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.editor.OPIEditorPerspective;
import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.csstudio.opibuilder.runmode.OPIShell;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class EditOPIAction extends Action implements IWorkbenchWindowActionDelegate {

    public static String ID = "org.csstudio.opibuilder.editor.edit";
    public static String ACTION_DEFINITION_ID = "org.csstudio.opibuilder.editopi";

    private static final String OPI_EDITOR_ID = "org.csstudio.opibuilder.OPIEditor";

    public EditOPIAction() {
        super("Display Builder", CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin("org.csstudio.opibuilder.editor", "icons/placeholder.gif"));
        setId(ID);
        setActionDefinitionId(ACTION_DEFINITION_ID);
    }

    @Override
    public void init(IWorkbenchWindow window) {
        // NOP
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // NOP
    }

    @Override
    public void run(IAction action) {
        run();
    }

    @Override
    public void run() {
        var page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        var shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IOPIRuntime opiRuntime = OPIShell.getOPIShellForShell(shell);
        if (opiRuntime == null) {
            // if the selected object isn't an OPIShell so grab the
            // OPIView or OPIRunner currently selected
            var part = page.getActivePart();
            if (part instanceof IOPIRuntime) {
                opiRuntime = (IOPIRuntime) part;
            }
        }

        IWorkbenchWindow targetWindow = null;
        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            if (window.getActivePage().getPerspective().getId().equals(OPIEditorPerspective.ID)) {
                targetWindow = window;
            }
        }
        if (targetWindow == null) {
            try {
                targetWindow = PlatformUI.getWorkbench().openWorkbenchWindow(OPIEditorPerspective.ID, null);
            } catch (WorkbenchException e) {
                throw new RuntimeException(e);
            }
        }

        targetWindow.getShell().setActive();
        if (opiRuntime != null) {
            var path = opiRuntime.getDisplayModel().getOpiFilePath();
            page = targetWindow.getActivePage();
            if (page != null) {
                try {
                    var editorInput = ResourceUtil.editorInputFromPath(path);
                    page.openEditor(editorInput, OPI_EDITOR_ID, true,
                            IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
                } catch (PartInitException e) {
                    ErrorHandlerUtil.handleError("Failed to open current OPI in editor", e);
                }
            }
        }
    }

    @Override
    public void dispose() {
        // NOP
    }
}
