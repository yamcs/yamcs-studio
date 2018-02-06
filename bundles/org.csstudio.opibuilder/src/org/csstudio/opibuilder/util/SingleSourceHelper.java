package org.csstudio.opibuilder.util;

import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.csstudio.opibuilder.widgetActions.OpenFileAction;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

public abstract class SingleSourceHelper {

    private static final SingleSourceHelper IMPL = new SingleSourceHelperImpl();

    public static void openOPIShell(IPath path, MacrosInput input) {
        if (IMPL != null)
            IMPL.iOpenOPIShell(path, input);
        else {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Not Implemented",
                    "Sorry, open OPI shell action is not implemented for WebOPI!");
        }
    }

    protected abstract void iOpenOPIShell(IPath path, MacrosInput input);

    public static IOPIRuntime getOPIShellForShell(Shell shell) {
        if (IMPL == null)
            return null;
        return IMPL.iGetOPIShellForShell(shell);
    }

    protected abstract IOPIRuntime iGetOPIShellForShell(Shell shell);

    public static GC getImageGC(final Image image) {
        if (IMPL == null)
            return null;
        return IMPL.iGetImageGC(image);
    }

    protected abstract GC iGetImageGC(final Image image);

    public static void openFileActionRun(OpenFileAction openFileAction) {
        if (IMPL != null)
            IMPL.iOpenFileActionRun(openFileAction);
        else {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Not Implemented",
                    "Sorry, open File action is not implemented for WebOPI!");
        }
    }

    protected abstract void iOpenFileActionRun(OpenFileAction openFileAction);

    public static void addPaintListener(final Control control, PaintListener paintListener) {
        if (IMPL != null)
            IMPL.iAddPaintListener(control, paintListener);
    }

    protected abstract void iAddPaintListener(Control control,
            PaintListener paintListener);

    public static void removePaintListener(final Control control, PaintListener paintListener) {
        if (IMPL != null)
            IMPL.iRemovePaintListener(control, paintListener);
    }

    protected abstract void iRemovePaintListener(Control control,
            PaintListener paintListener);

    public static void registerRCPRuntimeActions(
            ActionRegistry actionRegistry, IOPIRuntime opiRuntime) {
        if (IMPL != null)
            IMPL.iRegisterRCPRuntimeActions(actionRegistry, opiRuntime);
    }

    protected abstract void iRegisterRCPRuntimeActions(
            ActionRegistry actionRegistry, IOPIRuntime opiRuntime);

    public static void appendRCPRuntimeActionsToMenu(
            ActionRegistry actionRegistry, IMenuManager menu) {
        if (IMPL != null)
            IMPL.iappendRCPRuntimeActionsToMenu(actionRegistry, menu);
    }

    protected abstract void iappendRCPRuntimeActionsToMenu(
            ActionRegistry actionRegistry, IMenuManager menu);

    public static IPath rcpGetPathFromWorkspaceFileDialog(
            IPath startPath, String[] extensions) {
        if (IMPL != null)
            return IMPL.iRcpGetPathFromWorkspaceFileDialog(startPath, extensions);
        return null;
    }

    protected abstract IPath iRcpGetPathFromWorkspaceFileDialog(
            IPath startPath, String[] extensions);

    public static void openEditor(final IWorkbenchPage page, IPath path)
            throws Exception {
        if (IMPL != null)
            IMPL.iOpenEditor(page, path);
    }

    protected abstract void iOpenEditor(final IWorkbenchPage page, IPath path)
            throws Exception;

}
