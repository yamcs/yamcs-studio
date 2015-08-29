package org.yamcs.studio.ui.connections;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Pops up the connection manager dialog
 */
public class ConnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(ConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ConnectionsDialog dialog = new ConnectionsDialog(window.getShell());
        if (dialog.open() == Dialog.OK) {
            YamcsConfiguration conf = dialog.getChosenConfiguration();
            doConnect(conf, true);
        }

        return null;
    }

    private void doConnect(YamcsConfiguration conf, boolean noPasswordPopup) {
        // FIXME get the password out before doing this
        ConnectionPreferences.setLastUsedConfiguration(conf);
        // TODO this should instead pass the primary connection information to YamcsPlugin.
        //YamcsPlugin.getDefault().connect(null);
        System.out.println("Will NOW connect to " + conf.getName() + " w password " + conf.getPassword());
    }
}
