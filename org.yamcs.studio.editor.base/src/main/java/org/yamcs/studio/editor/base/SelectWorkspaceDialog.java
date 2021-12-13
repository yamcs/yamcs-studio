/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.editor.base;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class SelectWorkspaceDialog extends TitleAreaDialog {

    private List<String> recentWorkspaces;
    private Combo workspaces;

    private String selectedWorkspace;

    public SelectWorkspaceDialog() {
        super(null);
        this.recentWorkspaces = UserPreferences.readWorkspaceHistory();

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Platform.getProduct().getName());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parentComposite = (Composite) super.createDialogArea(parent);

        setTitle("Select Workspace");
        setMessage("The workspace directory is where " + Platform.getProduct().getName()
                + " will store your files and preferences.");

        // Create the layout
        var contents = new Composite(parent, SWT.NONE);
        var layout = new GridLayout(1, false);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        contents.setLayout(layout);
        contents.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
        contents.setFont(parent.getFont());

        createWorkspaceSection(contents);

        return parentComposite;
    }

    private void createWorkspaceSection(Composite parent) {
        var layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);
        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        parent.setLayoutData(gd);

        workspaces = new Combo(parent, SWT.DROP_DOWN);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        workspaces.setLayoutData(gd);
        // Fill w/ current workspace history, select the first one
        workspaces.setItems(recentWorkspaces.toArray(new String[0]));
        workspaces.select(0);

        var browse = new Button(parent, SWT.PUSH);
        browse.setText("Browse");
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                var dialog = new DirectoryDialog(getShell());
                dialog.setText("Select Workspace");
                dialog.setMessage("Select existing workspace");
                dialog.setFilterPath(getInitialBrowsePath());
                var dir = dialog.open();
                if (dir != null) {
                    workspaces.setText(dir);
                }
            }
        });
    }

    @Override
    protected void okPressed() {
        if (!checkWorkspace()) {
            return;
        }
        super.okPressed();
    }

    public String getSelectedWorkspace() {
        return selectedWorkspace;
    }

    /**
     * @return Directory name close to the currently entered workspace
     */
    private String getInitialBrowsePath() {
        var dir = new File(workspaces.getText());
        if (dir != null) { // Go one up
            dir = dir.getParentFile();
        }
        // Go further up until we find something that actually exists
        while ((dir != null) && !dir.exists()) {
            dir = dir.getParentFile();
        }
        if (dir == null) {
            return System.getProperty("user.dir");
        }
        return dir.getAbsolutePath();
    }

    /**
     * check if there is error in workspace input
     * 
     * @return true if there is no error
     */
    protected boolean checkWorkspace() {
        var workspace = workspaces.getText().trim();

        // Must not be empty
        if (workspace.length() <= 0) {
            setErrorMessage("Workspace field must not be empty; enter a path to continue.");
            return false;
        }

        // Check if this workspace is inside another workspace...
        var ws_file = new File(workspace);
        try {
            var parent = ws_file.getParentFile();
            while (parent != null) { // Is there a .metadata file?
                var meta = new File(parent.getCanonicalPath() + File.separator + ".metadata");
                if (meta.exists()) {
                    setErrorMessage(String.format(
                            "The selected directory is inside an existing workspace named \\\"%s\\\".\\nPick a directory that is neither inside an existing workspace, nor contains another workspace.",
                            parent.getName()));
                    return false;
                }
                // OK, go one up
                parent = parent.getParentFile();
            }
        } catch (IOException ex) {
            setErrorMessage("Error: " + ex.getMessage());
            return false;
        }

        // Check if there are already workspaces within the selected directory.
        var nested = checkForWorkspacesInSubdirs(ws_file);
        if (nested != null) {
            setErrorMessage(String.format(
                    "There is already a workspace named \\\"%s\\\" below the selected directory.\\nPick a directory that is neither inside an existing workspace, nor contains another workspace.",
                    nested));
            return false;
        }

        selectedWorkspace = workspace;
        return true;
    }

    /**
     * Check if directory or any subdirectory contains a workspace
     * 
     * @param dir
     *            Directory where to start
     * @return Name of workspace in subdir or <code>null</code> if none found
     * @throws Exception
     *             on error
     */
    private String checkForWorkspacesInSubdirs(File dir) {
        var subdirs = dir.listFiles();
        if (subdirs == null) {
            return null;
        }
        for (var subdir : subdirs) {
            if (!subdir.isDirectory()) {
                continue;
            }
            try { // Is there a .metadata file?
                var meta = new File(subdir.getCanonicalPath() + File.separator + ".metadata");
                if (meta.exists()) {
                    return subdir.getName();
                }
            } catch (Exception ex) {
                // Ignore errors. If there's a workspace we can't read, don't worry.
            }
        }
        return null;
    }
}
