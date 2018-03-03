package org.yamcs.studio.explorer;

import java.util.Optional;

import org.csstudio.opibuilder.runmode.RunModeService;
import org.csstudio.opibuilder.runmode.RunModeService.DisplayMode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.part.FileEditorInput;

@SuppressWarnings("restriction")
public class OpenFileAction extends Action implements IWorkbenchAction {

    private static final String OPI_BUILDER_PERSPECTIVE_ID = "org.csstudio.opibuilder.opieditor";
    private static final String OPI_RUNTIME_PERSPECTIVE_ID = "org.csstudio.opibuilder.OPIRuntime.perspective";

    private IWorkbenchPage page;
    private ISelectionProvider selectionProvider;

    public OpenFileAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
        super("Open");
        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void run() {
        IPerspectiveDescriptor activePerspective = page.getPerspective();
        IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
        for (Object o : selection.toList()) {
            if (o instanceof IFile) {
                IFile file = (IFile) o;
                if (activePerspective.getId().equals(OPI_RUNTIME_PERSPECTIVE_ID)) {
                    openFileInRuntimeMode(file);
                } else if (activePerspective.getId().equals(OPI_BUILDER_PERSPECTIVE_ID)) {
                    openFileInBuilderMode(file);
                } else {
                    openFile(file);
                }
            }
        }
    }

    private void openFileInRuntimeMode(IFile file) {
        if (file.getFileExtension().equalsIgnoreCase("opi")) {
            IPath workspacePath = file.getFullPath();
            // IPath workspacePath = LauncherHelper.systemPathToWorkspacePath(path);
            RunModeService.openDisplay(workspacePath, Optional.empty(), DisplayMode.NEW_TAB, Optional.empty());
        } else {
            boolean confirm = MessageDialog.openQuestion(null, "Problems Opening Editor", "This file cannot be opened"
                    + " in the OPI Runtime perspective. Do you wish to switch to the OPI Builder perspective?");
            if (confirm) {
                IWorkbench workbench = page.getWorkbenchWindow().getWorkbench();
                IPerspectiveDescriptor descriptor = workbench
                        .getPerspectiveRegistry()
                        .findPerspectiveWithId(OPI_BUILDER_PERSPECTIVE_ID);
                workbench.getActiveWorkbenchWindow().getActivePage().setPerspective(descriptor);
                openFile(file);
            }
        }
    }

    private void openFileInBuilderMode(IFile file) {
        if (file.getFileExtension().equalsIgnoreCase("opi")) {
            try {
                boolean activate = OpenStrategy.activateOnOpen();
                page.openEditor(new FileEditorInput(file), "org.csstudio.opibuilder.OPIEditor", activate,
                        IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
            } catch (PartInitException e) {
                DialogUtil.openError(page.getWorkbenchWindow().getShell(), "Problems Opening Editor", e.getMessage(),
                        e);
            }
        } else {
            openFile(file);
        }
    }

    private void openFile(IFile file) {
        try {
            boolean activate = OpenStrategy.activateOnOpen();
            IDE.openEditor(page, file, activate);
        } catch (PartInitException e) {
            DialogUtil.openError(page.getWorkbenchWindow().getShell(), "Problems Opening Editor", e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
    }
}
