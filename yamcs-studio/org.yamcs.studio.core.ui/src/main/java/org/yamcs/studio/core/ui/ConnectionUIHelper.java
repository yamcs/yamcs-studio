package org.yamcs.studio.core.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class ConnectionUIHelper implements StudioConnectionListener {

    private static final Logger log = Logger.getLogger(ConnectionUIHelper.class.getName());

    private static ConnectionUIHelper instance = new ConnectionUIHelper();

    private volatile boolean reconnecting;

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
            // Prevent triggering on automatic reconnect
            if (reconnecting) return;

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
    public void onStudioConnectionLost() {
        Display.getDefault().asyncExec(() -> {
            reconnecting = true;
            try {
                new ProgressMonitorDialog(null).run(true /* fork */, true /* cancel */, monitor -> {
                    monitor.beginTask("Connection Lost. Reconnecting...", IProgressMonitor.UNKNOWN);
                    while (!monitor.isCanceled()) {
                        try {
                            ConnectionManager.getInstance().connect().get();
                            monitor.done();
                            return;
                        } catch (CancellationException | ExecutionException | InterruptedException e) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    reconnecting = false;
                });
            } catch (InvocationTargetException e) {
                log.log(Level.SEVERE, "Failure while attempting to re-establish connection", e);
            } catch (InterruptedException e) {
                log.finest("Reconnection attempt interrupted");
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
