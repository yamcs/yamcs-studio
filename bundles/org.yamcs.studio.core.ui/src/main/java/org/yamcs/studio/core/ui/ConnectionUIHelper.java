package org.yamcs.studio.core.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.client.YamcsClient;
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

        YamcsClient yamcsClient = ConnectionManager.getInstance().getYamcsClient();
        YamcsConnectionProperties yprops = yamcsClient.getYamcsConnectionProperties();
        Display.getDefault().asyncExec(() -> {

            if (t.getMessage() != null && t.getMessage().contains("401")) {
                // Show Login Pane
                RCPUtils.runCommand("org.yamcs.studio.ui.login");
            } else {
                String detail = (t.getMessage() != null) ? t.getMessage() : t.getClass().getSimpleName();
                MessageDialog.openError(Display.getDefault().getActiveShell(), yprops.getUrl(),
                        "Could not connect. " + detail);
            }
        });
    }

    @Override
    public void onStudioConnect() {
    }

    @Override
    public void onStudioDisconnect() {
    }
}
