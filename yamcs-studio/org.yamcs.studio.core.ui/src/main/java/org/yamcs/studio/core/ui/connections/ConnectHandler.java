package org.yamcs.studio.core.ui.connections;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
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
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.security.YamcsCredentials;

/**
 * Pops up the connection manager dialog.
 *
 * TODO No path ever leads to opening the login dialog. That's good and all, but
 * it also means that we are not correctly setting the JAAS stuff. We must fix
 * this soon.
 */
public class ConnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(ConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ConnectionsDialog dialog = new ConnectionsDialog(window.getShell());
        if (dialog.open() == Dialog.OK) {
            YamcsConfiguration conf = dialog.getChosenConfiguration();
            doConnect(HandlerUtil.getActiveShell(event), conf);
        }

        return null;
    }

    private void doConnect(Shell shell, YamcsConfiguration conf) {
        // FIXME get the password out before doing this
        ConnectionPreferences.setLastUsedConfiguration(conf);
        ConnectionManager.getInstance().setConnectionInfo(conf.toConnectionInfo());

        String connectionString = conf.getPrimaryConnectionString();
        if (conf.isAnonymous()) {
            log.info("Will connect anonymously to " + connectionString);
            doConnectWithProgress(shell, null, connectionString);
        } else {
            log.info("Will connect as user '" + conf.getUser() + "' to " + connectionString);
            doConnectWithProgress(shell, conf.toYamcsCredentials(), connectionString);
        }
    }

    /*
     * TODO make this job cancellable
     */
    private void doConnectWithProgress(Shell shell, YamcsCredentials creds, String connectionString) {
        try {
            IRunnableWithProgress op = monitor -> {
                monitor.beginTask("Connecting to " + connectionString, IProgressMonitor.UNKNOWN);
                CompletableFuture<Void> future = ConnectionManager.getInstance().connect(creds);
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
