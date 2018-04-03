/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import java.util.Objects;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ConsoleService;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

/**
 * The action opening a file using its default editor.
 * 
 * @author Xihui Chen
 *
 */
public class OpenFileAction extends AbstractWidgetAction {

    public static final String PROP_PATH = "path";

    @Override
    protected void configureProperties() {
        addProperty(new FilePathProperty(
                PROP_PATH, "File Path", WidgetPropertyCategory.Basic, new Path(""),
                new String[] { "*" }));

    }

    @Override
    public ActionType getActionType() {
        return ActionType.OPEN_FILE;
    }

    @Override
    public void run() {
        UIJob job = new UIJob(getDescription()) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                // Open editor on new file.
                IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (dw == null)
                    return Status.OK_STATUS; // Not really OK..
                try {
                    IWorkbenchPage page = Objects.requireNonNull(dw.getActivePage());

                    IPath absolutePath = getPath();
                    if (!absolutePath.isAbsolute())
                        absolutePath = ResourceUtil.buildAbsolutePath(getWidgetModel(), absolutePath);

                    // Workspace file?
                    IFile file = ResourceUtil.getIFileFromIPath(absolutePath);
                    if (file != null) { // Clear the last-used-editor info to always get the default editor,
                                        // the one configurable via Preferences, General, Editors, File Associations,
                                        // and not whatever one user may have used last via Navigator's "Open With..".
                                        // Other cases below use a new, local file that won't have last-used-editor
                                        // info, yet
                        file.setPersistentProperty(IDE.EDITOR_KEY, null);
                        IDE.openEditor(page, file, true);
                    } else if (ResourceUtil.isExistingLocalFile(absolutePath)) { // Local file system
                        try {
                            IFileStore localFile = EFS.getLocalFileSystem().getStore(absolutePath);
                            IDE.openEditorOnFileStore(page, localFile);
                        } catch (Exception e) {
                            throw new Exception("Cannot open local file system location " + getPath(), e);
                        }
                    }
                } catch (Exception e) {
                    String message = "Failed to open file " + getPath();
                    ExceptionDetailsErrorDialog.openError(dw.getShell(), "Failed to open file", message, e);
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                    ConsoleService.getInstance().writeError(message);
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    public IPath getPath() {
        return (IPath) getPropertyValue(PROP_PATH);
    }

    @Override
    public String getDefaultDescription() {
        return super.getDefaultDescription() + " " + getPath(); //$NON-NLS-1$
    }

}
