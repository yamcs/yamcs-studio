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
 * Pops up the connection manager dialog.
 *
 * TODO No path ever leads to opening the login dialog. That's good and all, but it also means that
 * we are not correctly setting the JAAS stuff. We must fix this soon.
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

        String connectionString = conf.getPrimaryConnectionString();
        if (conf.isAnonymous()) {
            log.info("Will connect anonymously to " + connectionString);
            ConnectionManager.getInstance().connect(conf.toConnectionInfo(), null);
        } else {
            log.info("Will connect as user '" + conf.getUser() + "' to " + connectionString);
            ConnectionManager.getInstance().connect(conf.toConnectionInfo(), conf.toYamcsCredentials());
        }
    }
}
