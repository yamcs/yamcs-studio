package org.yamcs.studio.css.utility;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.csstudio.platform.workspace.WorkspaceInfo;
import org.csstudio.startup.module.WorkspaceExtPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;

public class WorkspacePrompt implements WorkspaceExtPoint {

    @Override
    public Object promptForWorkspace(Display display, IApplicationContext context, Map<String, Object> parameters) {
        boolean promptForWorkspace = (Boolean) parameters.get(StartupParameters.PARAM_PROMPT_FOR_WORKSPACE);
        URL suggestedWorkspace = (URL) parameters.get(StartupParameters.PARAM_SUGGESTED_WORKSPACE);

        if (!checkInstanceLocation(promptForWorkspace, suggestedWorkspace, parameters)) {
            // The stop() routine of many UI plugins writes the current settings to the workspace.
            // Even though we have not yet opened any workspace, that would open, even create
            // the default workspace. So exit right away:
            System.exit(0);
            // .. instead of:
            // Platform.endSplash();
            return IApplication.EXIT_OK;
        }
        return null;
    }

    /**
     * Check or select the workspace.
     * <p>
     * See IDEApplication code from org.eclipse.ui.internal.ide.application in version 3.3. That example uses a "Shell"
     * argument, but also has a comment about bug 84881 and thus not using the shell to force the dialogs to be
     * top-level, so we skip the shell altogether.
     * <p>
     * Note that we must be very careful with anything that sets the workspace. For example, initializing a logger from
     * preferences activates the default workspace, after which we can no longer change it...
     *
     * @return <code>true</code> if all OK
     */
    private boolean checkInstanceLocation(boolean forcePrompt, URL defaultWorkspace, Map<String, Object> parameters) {
        // Was "-data @none" specified on command line?
        Location instanceLoc = Platform.getInstanceLocation();

        if (instanceLoc == null) {
            MessageDialog.openError(null, "No workspace", "Cannot run without a workspace");
            return false;
        }

        // -data "/some/path" was provided...
        if (instanceLoc.isSet()) {
            try { // Lock
                if (instanceLoc.lock()) {
                    return true;
                }
                // Two possibilities:
                // 1. directory is already in use
                // 2. directory could not be created
                File ws_dir = new File(instanceLoc.getURL().getFile());
                if (ws_dir.exists()) {
                    MessageDialog.openError(null, "Workspace in use",
                            String.format("Workspace %s is in use. Select a different workspace",
                                    ws_dir.getCanonicalPath()));
                } else {
                    MessageDialog.openError(null, "Directory error", "File permission error with workspace directory");
                }
            } catch (IOException ex) {
                MessageDialog.openError(null, "Workspace lock error", "Cannot lock workspace: " + ex.getMessage());
            }
            return false;
        }

        // -data @noDefault or -data not specified, prompt and set
        if (defaultWorkspace == null) {
            defaultWorkspace = instanceLoc.getDefault();
        }

        WorkspaceInfo workspaceInfo = new WorkspaceInfo(defaultWorkspace, !forcePrompt);

        // Prompt in any case? Or did user decide to be asked again?
        boolean showWorkspace = forcePrompt | workspaceInfo.getShowDialog();

        while (true) {
            if (showWorkspace) {
                String message = String.format(
                        "Select your %s workspace, where your files, preferences etc. will be stored.",
                        Platform.getProduct().getName());

                WorkspaceDialog dialog = new WorkspaceDialog("Select Workspace", message, workspaceInfo, !forcePrompt);
                if (dialog.open() == WorkspaceDialog.CANCEL) {
                    return false;
                }
            }
            // In case of errors, we will have to ask the workspace
            showWorkspace = true;

            try {
                // the operation will fail if the url is not a valid
                // instance data area, so other checking is unneeded
                URL workspaceUrl = new URL("file:" + workspaceInfo.getSelectedWorkspace());
                if (instanceLoc.set(workspaceUrl, true)) { // set & lock
                    workspaceInfo.writePersistedData();
                    parameters.put(WORKSPACE, workspaceUrl);
                    return true;
                }
            } catch (Exception e) {
                MessageDialog.openError(null, "Workspace error", "Cannot set workspace: " + e.getMessage());
                return false;
            }
            // by this point it has been determined that the workspace is
            // already in use -- force the user to choose again
            MessageDialog.openError(null, "Workspace in use", String.format(
                    "Workspace %s is in use. Select a different workspace", workspaceInfo.getSelectedWorkspace()));
        }
    }
}
