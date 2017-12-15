package org.yamcs.studio.css.utility;

import java.io.File;
import java.io.IOException;

import org.csstudio.platform.workspace.Messages;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
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

    private String[] recentWorkspaces;
    private Combo workspaces;

    private String selectedWorkspace;

    public SelectWorkspaceDialog(String... recentWorkspaces) {
        super(null);
        this.recentWorkspaces = recentWorkspaces;

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Platform.getProduct().getName());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        setTitle("Select Workspace");
        setMessage("The workspace directory is where " + Platform.getProduct().getName()
                + " will store your files and preferences.");

        // Create the layout
        Composite contents = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
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
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        parent.setLayoutData(gd);

        workspaces = new Combo(parent, SWT.DROP_DOWN);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        workspaces.setLayoutData(gd);
        // Fill w/ current workspace history, select the first one
        workspaces.setItems(recentWorkspaces);
        workspaces.select(0);

        Button browse = new Button(parent, SWT.PUSH);
        browse.setText("Browse");
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                dialog.setText("Select Workspace");
                dialog.setMessage("Select existing workspace");
                dialog.setFilterPath(getInitialBrowsePath());
                String dir = dialog.open();
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
        File dir = new File(workspaces.getText());
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
        String workspace = workspaces.getText().trim();

        // Must not be empty
        if (workspace.length() <= 0) {
            setErrorMessage("Workspace field must not be empty; enter a path to continue.");
            return false;
        }

        // Check if this workspace is inside another workspace...
        final File ws_file = new File(workspace);
        try {
            File parent = ws_file.getParentFile();
            while (parent != null) { // Is there a .metadata file?
                File meta = new File(parent.getCanonicalPath() + File.separator + ".metadata");
                if (meta.exists()) {
                    setErrorMessage(NLS.bind(Messages.Workspace_NestedErrorFMT, parent.getName()));
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
        String nested = checkForWorkspacesInSubdirs(ws_file);
        if (nested != null) {
            setErrorMessage(NLS.bind(Messages.Workspace_ContainsWorkspacesErrorFMT, nested));
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
        File subdirs[] = dir.listFiles();
        if (subdirs == null)
            return null;
        for (File subdir : subdirs) {
            if (!subdir.isDirectory())
                continue;
            try { // Is there a .metadata file?
                File meta = new File(subdir.getCanonicalPath() + File.separator + ".metadata");
                if (meta.exists())
                    return subdir.getName();
            } catch (Exception ex) {
                // Ignore errors. If there's a workspace we can't read, don't worry.
            }
        }
        return null;
    }
}
