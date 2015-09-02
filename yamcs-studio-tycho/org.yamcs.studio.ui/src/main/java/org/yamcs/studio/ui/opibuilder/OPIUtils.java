package org.yamcs.studio.ui.opibuilder;

import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.csstudio.opibuilder.runmode.OPIView;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OPIUtils {

    public static void resetDisplays() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            for (IWorkbenchPage page : window.getPages()) {
                for (IViewReference reference : page.getViewReferences()) {
                    IViewPart viewPart = reference.getView(false);
                    if (viewPart instanceof IOPIRuntime)
                        refreshDisplay((IOPIRuntime) viewPart);
                }
                for (IEditorReference reference : page.getEditorReferences()) {
                    IEditorPart editorPart = reference.getEditor(false);
                    if (editorPart instanceof IOPIRuntime)
                        refreshDisplay((IOPIRuntime) editorPart);
                }
            }
        }
    }

    private static void refreshDisplay(IOPIRuntime opiRuntime) {
        try {
            OPIView.ignoreMemento();
            opiRuntime.setOPIInput(opiRuntime.getOPIInput());
        } catch (PartInitException e) {
            ErrorHandlerUtil.handleError("Failed to refresh OPI", e);
        }
    }
}
