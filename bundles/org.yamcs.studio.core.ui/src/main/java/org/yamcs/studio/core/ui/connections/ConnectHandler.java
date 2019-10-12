package org.yamcs.studio.core.ui.connections;

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

/**
 * Pops up the connection manager dialog.
 */
public class ConnectHandler extends AbstractHandler {

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

        YamcsConnectionProperties yprops = conf.getConnectionProperties();

        ConnectionUIHelper.connectWithProgressDialog(shell, yprops);
    }
}
