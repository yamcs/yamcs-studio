package org.yamcs.studio.ui.connections;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.core.ConnectionManager;

/**
 * Does a connection on the last-used configuration, with potential UI interactions if a password is
 * required and this password was not saved to disk.
 * <p>
 * If there is no last-used configuration, yet this method was called, it pops up the richer
 * connection manager dialog.
 */
public class AutoConnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(AutoConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        log.info("Autoconnecting");
        YamcsConfiguration conf = ConnectionPreferences.getLastUsedConfiguration();
        if (conf != null) {
            log.info("Found previous configuration '" + conf.getName() + "'");
            doConnect(shell, conf, false);
        } else {
            log.info("No previous configuration. Open Connections dialog");
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ConnectionsDialog dialog = new ConnectionsDialog(window.getShell());
            if (dialog.open() == Dialog.OK) {
                conf = dialog.getChosenConfiguration();
                doConnect(shell, conf, true);
            } else {
                log.info("Connection attempt cancelled by user");
            }
        }

        return null;
    }

    private void doConnect(Shell shell, YamcsConfiguration conf, boolean noPasswordPopup) {
        // FIXME get the password out before doing this
        ConnectionPreferences.setLastUsedConfiguration(conf);

        // Check if authentication is needed
        String connectionString = conf.getPrimaryConnectionString();
        if (conf.isAnonymous()) {
            log.info("Will connect anonymously to " + connectionString);
            ConnectionManager.getInstance().connect(conf.toConnectionInfo(), null);
        } else if (conf.isSavePassword() || noPasswordPopup) {
            log.info("Will connect as user '" + conf.getUser() + "' to " + connectionString);
            ConnectionManager.getInstance().connect(conf.toConnectionInfo(), conf.toYamcsCredentials());
        } else {
            log.info("Want to connect to '" + connectionString
                    + "' but credentials are needed (not saved and not in dialog). Show password dialog");
            new LoginDialog(shell, conf).open();
        }
    }
}
