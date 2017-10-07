package org.yamcs.studio.core.ui.connections;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

/**
 * Pops up the connection manager dialog. Except when single-connection mode is activated.
 * That will bypass the dialog for a configurable Yamcs connection string.
 *
 * @todo verify auth on single conn mode
 */
public class ConnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(ConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        boolean singleConnectionMode = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("singleConnectionMode");
        if (singleConnectionMode) {
            String connectionString = YamcsUIPlugin.getDefault().getPreferenceStore().getString("connectionString");
            try {
                YamcsConnectionProperties primaryProps = YamcsConnectionProperties.parse(connectionString);
                ConnectionManager.getInstance().setConnectionInfo(new ConnectionInfo(primaryProps, null));
                doConnectWithProgress(HandlerUtil.getActiveShell(event), connectionString);
            } catch (URISyntaxException e) {
                log.log(Level.SEVERE, "Invalid URL", e);
                return null;
            }
        } else {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ConnectionsDialog dialog = new ConnectionsDialog(window.getShell());
            if (dialog.open() == Dialog.OK) {
                YamcsConfiguration conf = dialog.getChosenConfiguration();
                doConnect(HandlerUtil.getActiveShell(event), conf);
            }
        }

        return null;
    }

    private void doConnect(Shell shell, YamcsConfiguration conf) {
        // FIXME get the password out before doing this
        ConnectionPreferences.setLastUsedConfiguration(conf);
        ConnectionManager.getInstance().setConnectionInfo(conf.toConnectionInfo());

        String connectionString = conf.getPrimaryConnectionString();
        log.info("Will connect to " + connectionString);
        doConnectWithProgress(shell, connectionString);
    }

    /*
     * TODO make this job cancellable
     */
    private void doConnectWithProgress(Shell shell, String connectionString) {
        try {
            IRunnableWithProgress op = monitor -> {
                monitor.beginTask("Connecting to " + connectionString, IProgressMonitor.UNKNOWN);
                CompletableFuture<Void> future = ConnectionManager.getInstance().connect();
                future.whenComplete((ret, ex) -> {
                    monitor.done();
                });
            };
            // should not need to fork, but ws client currently blocks a bit :(
            new ProgressMonitorDialog(shell).run(true /* fork */, false /* cancel */, op);
        } catch (InvocationTargetException e) {
            MessageDialog.openError(shell, "Failed to connect", e.getMessage());
        } catch (InterruptedException e) {
            log.info("Connection attempt cancelled");
        }
    }
}
