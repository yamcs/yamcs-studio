package org.yamcs.studio.css.utility;

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

public class YamcsStudioWorkspace implements WorkspaceExtPoint {

    /*-
     * In PDE/UI the default generated launch configuration will have the -data attribute set.
     * When testing code changes use these Eclipse parameters in your launch configuration:
     * 
     * - Start without support for a workspace: -data @none
     * - Start without specifying a default path: -data @noDefault
     * - Start with a preconfigured workspace: -data /some/workspace
     */

    @Override
    public Object promptForWorkspace(Display display, IApplicationContext context, Map<String, Object> parameters) {
        try {
            if (selectWorkspace(parameters)) {
                return null; // null means success
            }
        } catch (IOException e) {
            MessageDialog.openError(null, "Workspace Error", "Cannot set workspace: " + e.getMessage());
        }

        System.exit(0);
        return IApplication.EXIT_OK;
    }

    private boolean selectWorkspace(Map<String, Object> parameters) throws IOException {
        Location dataLocation = Platform.getInstanceLocation();
        if (dataLocation == null) { // -data @none
            MessageDialog.openError(null, "No workspace", Platform.getProduct().getName() + " requires a workspace");
            return false;
        } else if (dataLocation.isSet()) { // -data /some/workspace
            // This is set when using the built-in -data parameter. In general we recommend
            // the use of -workspace instead of -data because with -data the platform
            // location cannot be changed.
            // -data is however automatically set in the PDE configuration, so we do support it.
            // It then basically works as a kill witch for any workspace selection.
            dataLocation.lock();
            return true;
        }

        // Determine workspace
        URL workspaceUrl = (URL) parameters.get(YamcsStudioStartupParameters.PARAM_WORKSPACE);
        boolean forcePrompt = (Boolean) parameters.get(YamcsStudioStartupParameters.PARAM_WORKSPACE_PROMPT);
        return selectAndLockWorkspace(workspaceUrl, forcePrompt);
    }

    private boolean selectAndLockWorkspace(URL workspaceSuggestion, boolean promptUser) throws IOException {
        WorkspaceInfo workspaceInfo = new WorkspaceInfo(workspaceSuggestion, false);
        URL workspaceUrl = workspaceSuggestion;
        while (true) {
            if (promptUser) {
                String[] recentWorkspaces = getRecentWorkspaces(workspaceInfo);
                SelectWorkspaceDialog dialog = new SelectWorkspaceDialog(recentWorkspaces);
                if (dialog.open() == SelectWorkspaceDialog.CANCEL) {
                    return false;
                } else {
                    String selectedWorkspace = dialog.getSelectedWorkspace();

                    // Update 'recent workspaces' list
                    workspaceInfo.setSelectedWorkspace(selectedWorkspace);
                    workspaceUrl = new URL("file:" + selectedWorkspace);
                }
            }

            // Lock workspace
            if (Platform.getInstanceLocation().set(workspaceUrl, true)) {
                workspaceInfo.writePersistedData();
                return true;
            } else {
                MessageDialog.openError(null, "Workspace Error", String.format(
                        "Workspace %s is in use or cannot be accessed. Select a different workspace.",
                        workspaceUrl.getPath()));
            }
        }
    }

    private static String[] getRecentWorkspaces(WorkspaceInfo workspaceInfo) {
        String[] recentWorkspaces = new String[workspaceInfo.getWorkspaceCount()];
        for (int i = 0; i < recentWorkspaces.length; i++) {
            recentWorkspaces[i] = workspaceInfo.getWorkspace(i);
        }
        return recentWorkspaces;
    }
}
