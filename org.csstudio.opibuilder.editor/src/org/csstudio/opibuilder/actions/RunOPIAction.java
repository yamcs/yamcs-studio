/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editor.OPIEditor;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.runmode.OPIRunnerPerspective;
import org.csstudio.opibuilder.runmode.OPIView;
import org.csstudio.opibuilder.runmode.RunModeService;
import org.csstudio.opibuilder.runmode.RunModeService.DisplayMode;
import org.csstudio.opibuilder.runmode.RunnerInput;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Action to launch OPI runtime from editor.
 *
 * <p>
 * Maintains a workbench window to allow testing displays in their own top-level workbench, separate from the one that
 * holds the editor.
 *
 * @author Xihui Chen - Original author.
 * @author Kay Kasemir
 */
public class RunOPIAction extends Action implements IWorkbenchWindowActionDelegate {

    public static String ID = "org.csstudio.opibuilder.editor.run";
    public static String ACTION_DEFINITION_ID = "org.csstudio.opibuilder.runopi";

    public RunOPIAction() {
        super("Display Runner", CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(
                OPIBuilderPlugin.PLUGIN_ID, "icons/run_exc.png"));
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
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            IEditorPart activeEditor = page.getActiveEditor();
            if (activeEditor != null) {
                // TODO: Should perform the 'save' in background, then return to UI thread when done..
                if (PreferencesHelper.isAutoSaveBeforeRunning() && activeEditor.isDirty()) {
                    activeEditor.doSave(null);
                }
            }

            IWorkbenchWindow targetWindow = null;
            for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                if (window.getActivePage().getPerspective().getId().equals(OPIRunnerPerspective.ID)) {
                    targetWindow = window;
                }
            }

            if (targetWindow == null) {
                IWorkbenchPage runnerPage = RunModeService.createNewWorkbenchPage(Optional.empty());
                targetWindow = runnerPage.getWorkbenchWindow();
            }

            targetWindow.getShell().setActive();

            if (activeEditor instanceof OPIEditor) {
                // DisplayModel displayModel = ((OPIEditor) activeEditor).getDisplayModel();
                // Rectangle bounds = new Rectangle(displayModel.getLocation(), displayModel.getSize());

                IEditorInput input = activeEditor.getEditorInput();
                IPath path = ResourceUtil.getPathInEditor(input);
                RunnerInput new_input = new RunnerInput(path, null);

                // If this display is already executing, update it to the new content,
                // because RunModeService would only pop old content back to the front.
                for (IViewReference view_ref : targetWindow.getActivePage().getViewReferences()) {
                    if (view_ref.getId().startsWith(OPIView.ID)) {
                        IViewPart view = view_ref.getView(true);
                        if (view instanceof OPIView) {
                            OPIView opi_view = (OPIView) view;
                            if (new_input.equals(opi_view.getOPIInput())) {
                                try {
                                    opi_view.setOPIInput(new_input);
                                } catch (PartInitException ex) {
                                    OPIBuilderPlugin.getLogger().log(Level.WARNING,
                                            "Failed to update existing runtime for " + new_input.getName(), ex);
                                }
                                break;
                            }
                        }
                    }
                }

                RunModeService.openDisplayInView(targetWindow.getActivePage(), new_input, DisplayMode.NEW_TAB);
            }
        } catch (Exception ex) {
            ExceptionDetailsErrorDialog.openError(page.getWorkbenchWindow().getShell(),
                    "Cannot launch display runtime", ex);
        }
    }

    @Override
    public void dispose() {
    }
}
