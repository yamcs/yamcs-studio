package org.yamcs.studio.core.ui.connections;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.ui.ConnectionUIHelper;

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
        YamcsConnectionProperties yprops = conf.getConnectionProperties();
        if (conf.isAnonymous()) {
            log.fine("Will connect anonymously to " + yprops);
            YamcsPlugin.getYamcsClient().connect(yprops);
        } else if (conf.isSavePassword() || noPasswordPopup) {
            log.fine("Will connect as user '" + conf.getUser() + "' to " + yprops);
            YamcsPlugin.getYamcsClient().connect(yprops);
        } else {
            log.fine("Want to connect to '" + yprops
                    + "' but credentials are needed (not saved and not in dialog). Show password dialog");
            LoginDialog dialog = new LoginDialog(shell, conf);
            if (dialog.open() == Dialog.OK) {
                conf.setUser(dialog.getUser());
                conf.setPassword(dialog.getPassword());
                ConnectionUIHelper.connectWithProgressDialog(shell, yprops);
            }
        }
    }
}
