package org.csstudio.opibuilder.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class RefreshAllDisplaysHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(RefreshAllDisplaysHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            for (IWorkbenchPage page : window.getPages()) {
                for (IViewReference reference : page.getViewReferences()) {
                    IViewPart viewPart = reference.getView(false);
                    if (viewPart instanceof IOPIRuntime) {
                        refreshDisplay((IOPIRuntime) viewPart);
                    }
                }
                for (IEditorReference reference : page.getEditorReferences()) {
                    IEditorPart editorPart = reference.getEditor(false);
                    if (editorPart instanceof IOPIRuntime) {
                        refreshDisplay((IOPIRuntime) editorPart);
                    }
                }
            }
        }
        return null;
    }

    private static void refreshDisplay(IOPIRuntime opiRuntime) {
        try {
            opiRuntime.setOPIInput(opiRuntime.getOPIInput());
        } catch (PartInitException e) {
            log.log(Level.SEVERE, "Failed to refresh OPI", e);
        }
    }
}
