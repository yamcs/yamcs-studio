package org.yamcs.studio.core.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.ConnectionMode;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class ConnectionUIHelper implements StudioConnectionListener {

    private static ConnectionUIHelper instance = new ConnectionUIHelper();

    public ConnectionUIHelper() {
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    public static ConnectionUIHelper getInstance() {
        return instance;
    }

    @Override
    public void onStudioConnectionFailure(Throwable t) {
        YamcsConnectionProperties yprops = ConnectionManager.getInstance().getConnectionProperties();
        Display.getDefault().asyncExec(() -> {
            if (t.getMessage().contains("401")) {
                // Show Login Pane
                RCPUtils.runCommand("org.yamcs.studio.ui.login");
            } else {
                String detail = (t.getMessage() != null) ? t.getMessage() : t.getClass().getSimpleName();
                MessageDialog.openError(Display.getDefault().getActiveShell(), yprops.getUrl("http"),
                        "Could not connect. " + detail);
                // TODO attempt failover
                // askSwitchNode(errorMessage);
            }
        });
    }

    @Override
    public void onStudioConnect() {

    }

    @Override
    public void onStudioDisconnect() {
        // We should make this optional i think. It can be quite annoying during
        // development
        // YamcsConnectionProperties yprops =
        // ConnectionManager.getInstance().getWebProperties();
        // String connectionString = yprops.getYamcsConnectionString();
        // MessageDialog.openWarning(null, connectionString, "You are no longer
        // connected to Yamcs");
    }

    private void askSwitchNode(String errorMessage) {
        YamcsConnectionProperties yprops = ConnectionManager.getInstance().getConnectionProperties();
        String connectionString = yprops.getUrl("http");
        String message = "Connection error with " + connectionString;
        if (errorMessage != null && errorMessage != "") {
            message += "\nDetails:" + errorMessage;
        }

        ConnectionManager connectionManager = ConnectionManager.getInstance();
        if (connectionManager.getConnectionInfo().getConnection(ConnectionMode.FAILOVER) != null) {
            message += "\n\n" + "Do you want to switch to the failover server?";
            MessageDialog dialog = new MessageDialog(null, "Connection Error", null, message, MessageDialog.QUESTION,
                    new String[] { "Yes", "No" }, 0);

            if (dialog.open() == Dialog.OK)
                connectionManager.switchNode();
        }
    }
}
