package org.yamcs.studio.editor.base;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;

public class YamcsStudioWorkspace {

    /*-
     * In PDE/UI the default generated launch configuration will have the -data attribute set.
     * When testing code changes use these Eclipse parameters in your launch configuration:
     * 
     * - Start without support for a workspace: -data @none
     * - Start without specifying a default path: -data @noDefault
     * - Start with a preconfigured workspace: -data /some/workspace
     */

    public static boolean prompt(URL workspaceUrl, boolean promptUser) {
        try {
            return selectWorkspace(workspaceUrl, promptUser);
        } catch (IOException e) {
            MessageDialog.openError(null, "Workspace Error", "Cannot set workspace: " + e.getMessage());
            return false;
        }
    }

    private static boolean selectWorkspace(URL workspaceUrl, boolean forcePrompt) throws IOException {
        var dataLocation = Platform.getInstanceLocation();
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

        return selectAndLockWorkspace(workspaceUrl, forcePrompt);
    }

    private static boolean selectAndLockWorkspace(URL workspaceSuggestion, boolean promptUser) throws IOException {

        var workspaceUrl = workspaceSuggestion;
        while (true) {
            if (promptUser) {
                var dialog = new SelectWorkspaceDialog();
                if (dialog.open() == SelectWorkspaceDialog.CANCEL) {
                    return false;
                } else {
                    var selectedWorkspace = dialog.getSelectedWorkspace();

                    UserPreferences.updateWorkspaceHistory(selectedWorkspace);
                    workspaceUrl = new URL("file:" + selectedWorkspace);
                }
            }

            // Lock workspace
            if (Platform.getInstanceLocation().set(workspaceUrl, true)) {
                UserPreferences.updateWorkspaceHistory(workspaceUrl.getFile());
                return true;
            } else {
                MessageDialog.openError(null, "Workspace Error",
                        String.format("Workspace %s is in use or cannot be accessed. Select a different workspace.",
                                workspaceUrl.getPath()));
                promptUser = true;
            }
        }
    }
}
