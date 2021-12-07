package org.yamcs.studio.editor.base;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

//See org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction
public class SwitchWorkspaceAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public SwitchWorkspaceAction(IWorkbenchWindow window) {
        super("Switch &Workspace...");

        this.window = window;
    }

    @Override
    public void run() {
        var path = promptForWorkspace();
        if (path == null) {
            return;
        }

        UserPreferences.updateWorkspaceHistory(path);
        restart(path);
    }

    private void restart(String path) {
        var commandline = buildCommandLine(path);
        System.setProperty("eclipse.exitcode", IApplication.EXIT_RELAUNCH.toString());
        System.setProperty("eclipse.exitdata", commandline);
        window.getWorkbench().restart();
    }

    private String promptForWorkspace() {
        var dialog = new SelectWorkspaceDialog();
        if (dialog.open() == SelectWorkspaceDialog.OK) {
            return dialog.getSelectedWorkspace();
        } else {
            return null;
        }
    }

    /**
     * Create a command line that will launch a new workbench that is the same as the currently running one, but using
     * the argument directory as its workspace.
     *
     * @param workspace
     *            Directory to use as the new workspace
     * @return New command line or <code>null</code> on error
     */
    private String buildCommandLine(String workspace) {
        var property = System.getProperty("eclipse.vm");
        if (property == null) {
            MessageDialog.openError(null, "Error",
                    "Cannot determine virtual machine, need 'eclipse.vm ...' command-line argument\n"
                            + "Workspace switch does not work when started from within IDE!");
            return null;
        }

        var buf = new StringBuffer(512);
        buf.append(property);
        buf.append("\n");

        // append the vmargs and commands. Assume that these already end in \n
        var vmargs = System.getProperty("eclipse.vmargs");
        if (vmargs != null) {
            buf.append(vmargs);
        }

        // append the rest of the args, replacing or adding -data as required
        property = System.getProperty("eclipse.commands");
        if (property == null) {
            buf.append("-data");
            buf.append("\n");
            buf.append(workspace);
            buf.append("\n");
        } else {
            // find the index of the arg to replace its value
            var cmd_data_pos = property.lastIndexOf("-data");
            if (cmd_data_pos != -1) {
                cmd_data_pos += "-data".length() + 1;
                buf.append(property.substring(0, cmd_data_pos));
                buf.append(workspace);
                buf.append(property.substring(property.indexOf('\n', cmd_data_pos)));
            } else {
                buf.append("-data");
                buf.append("\n");
                buf.append(workspace);
                buf.append("\n");
                buf.append(property);
            }
        }

        // put the vmargs back at the very end (the eclipse.commands property
        // already contains the -vm arg)
        if (vmargs != null) {
            buf.append("-vmargs");
            buf.append("\n");
            buf.append(vmargs);
        }

        return buf.toString();
    }

    @Override
    public void dispose() {
        window = null;
    }
}
