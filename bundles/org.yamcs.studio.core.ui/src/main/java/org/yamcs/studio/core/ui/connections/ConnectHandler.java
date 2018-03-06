package org.yamcs.studio.core.ui.connections;

import java.net.URISyntaxException;
import java.util.logging.Level;
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
import org.yamcs.studio.core.ui.ConnectionUIHelper;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

/**
 * Pops up the connection manager dialog. Except when single-connection mode is activated. That will bypass the dialog
 * for a configurable Yamcs connection string.
 *
 * @todo verify auth on single conn mode
 */
public class ConnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(ConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        boolean singleConnectionMode = YamcsUIPlugin.getDefault().getPreferenceStore()
                .getBoolean("singleConnectionMode");
        if (singleConnectionMode) {
            String connectionString = YamcsUIPlugin.getDefault().getPreferenceStore().getString("connectionString");
            try {
                Shell shell = HandlerUtil.getActiveShell(event);
                YamcsConnectionProperties yprops = YamcsConnectionProperties.parse(connectionString);
                ConnectionUIHelper.connectWithProgressDialog(shell, yprops);
            } catch (URISyntaxException e) {
                log.log(Level.SEVERE, "Invalid URL " + connectionString, e);
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

        YamcsConnectionProperties yprops = conf.getPrimaryConnectionProperties();
        ConnectionUIHelper.connectWithProgressDialog(shell, yprops);
    }
}
