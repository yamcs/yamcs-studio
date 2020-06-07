package org.yamcs.studio.core.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsConfiguration;
import org.yamcs.studio.core.client.YamcsStudioClient;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class ConnectionUIHelper implements YamcsConnectionListener {

    private static final Logger log = Logger.getLogger(ConnectionUIHelper.class.getName());

    private static ConnectionUIHelper instance = new ConnectionUIHelper();

    public ConnectionUIHelper() {
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
    }

    public static ConnectionUIHelper getInstance() {
        return instance;
    }

    @Override
    public void onYamcsConnectionFailed(Throwable t) {
        Display.getDefault().asyncExec(() -> {
            if (t.getMessage() != null && t.getMessage().contains("401")) {
                // Show Login Pane
                RCPUtils.runCommand("org.yamcs.studio.ui.login");
            }
        });
    }

    @Override
    public void onYamcsConnected() {
    }

    @Override
    public void onYamcsDisconnected() {
    }

    public static void connectWithProgressDialog(Shell shell, YamcsConfiguration yprops) {
        try {
            YamcsUIConnector connector = new YamcsUIConnector(shell, yprops);
            new ProgressMonitorDialog(shell).run(true, true, connector);
        } catch (InvocationTargetException e) {
            log.log(Level.SEVERE, "Failed to connect", e);
            MessageDialog.openError(shell, "Failed to connect", e.getMessage());
        } catch (InterruptedException e) {
            log.info("Connection attempt cancelled");
        }
    }

    private static class YamcsUIConnector implements IRunnableWithProgress {

        private Shell shell;
        private YamcsConfiguration yprops;

        YamcsUIConnector(Shell shell, YamcsConfiguration yprops) {
            this.shell = shell;
            this.yprops = yprops;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask("Connecting to " + yprops, IProgressMonitor.UNKNOWN);
            YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
            try {
                Future<Void> future = yamcsClient.connect(yprops);
                RCPUtils.monitorCancellableFuture(monitor, future);
            } catch (ExecutionException e) {
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(shell, "Failed to connect", e.getCause().getMessage());
                });
            }
            monitor.done();
        }
    }
}
