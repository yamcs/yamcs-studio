package org.yamcs.studio.core.ui.connections;

import java.lang.reflect.InvocationTargetException;
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
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.ConnectionMode;
import org.yamcs.studio.core.client.YamcsClient;

/**
 * Does a connection on the last-used configuration, with potential UI interactions if a password is required and this
 * password was not saved to disk.
 * <p>
 * If there is no last-used configuration, yet this method was called, it pops up the richer connection manager dialog.
 */
public class AutoConnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(AutoConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        log.fine("Attempting Autoconnect");
        YamcsConfiguration conf = ConnectionPreferences.getLastUsedConfiguration();
        if (conf != null) {
            log.fine("Found previous configuration '" + conf.getName() + "'");
            doConnect(shell, conf, false);
        } else {
            log.fine("No previous configuration. Open Connections dialog");
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ConnectionsDialog dialog = new ConnectionsDialog(window.getShell());
            if (dialog.open() == Dialog.OK) {
                conf = dialog.getChosenConfiguration();
                doConnect(shell, conf, true);
            } else {
                log.fine("Connection attempt cancelled by user");
            }
        }

        return null;
    }

    private void doConnect(Shell shell, YamcsConfiguration conf, boolean noPasswordPopup) {
        // FIXME get the password out before doing this
        ConnectionPreferences.setLastUsedConfiguration(conf);

        // Check if authentication is needed
        YamcsConnectionProperties yprops = conf.toConnectionInfo().getConnection(ConnectionMode.PRIMARY);
        if (conf.isAnonymous()) {
            log.fine("Will connect anonymously to " + yprops);
            doConnectWithProgress(shell, yprops);
        } else if (conf.isSavePassword() || noPasswordPopup) {
            log.fine("Will connect as user '" + conf.getUser() + "' to " + yprops);
            doConnectWithProgress(shell, yprops);
        } else {
            log.fine("Want to connect to '" + yprops
                    + "' but credentials are needed (not saved and not in dialog). Show password dialog");
            LoginDialog dialog = new LoginDialog(shell, conf);
            if (dialog.open() == Dialog.OK) {
                conf.setUser(dialog.getUser());
                conf.setPassword(dialog.getPassword());
                doConnectWithProgress(shell, yprops);
            }
        }
    }

    /*
     * TODO this is same code as in ConnectHandler, can we just call that one's command?
     */
    private void doConnectWithProgress(Shell shell, YamcsConnectionProperties yprops) {
        try {
            IRunnableWithProgress op = monitor -> {
                monitor.beginTask("Connecting to " + yprops, IProgressMonitor.UNKNOWN);
                YamcsClient yamcsClient = ConnectionManager.getInstance().getYamcsClient();
                try {
                    log.info("Blocking connect...");
                    yamcsClient.connect(yprops).get();
                    log.info("Blocking connect finished..");
                } catch (java.util.concurrent.ExecutionException e) {
                    MessageDialog.openError(shell, "Failed to connect", e.getMessage());
                }
                monitor.done();
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
