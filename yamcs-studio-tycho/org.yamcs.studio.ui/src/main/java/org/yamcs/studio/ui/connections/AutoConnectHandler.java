package org.yamcs.studio.ui.connections;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;

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
        YamcsConfiguration conf = ConnectionPreferences.getLastUsedConfiguration();
        if (conf != null) {
            doConnect(conf, false);
        } else {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ConnectionsDialog dialog = new ConnectionsDialog(window.getShell());
            if (dialog.open() == Dialog.OK) {
                conf = dialog.getChosenConfiguration();
                doConnect(conf, true);
            }
        }

        return null;
    }

    private void doConnect(YamcsConfiguration conf, boolean noPasswordPopup) {
        // FIXME get the password out before doing this
        ConnectionPreferences.setLastUsedConfiguration(conf);

        // Check if authentication is needed
        if (conf.isSavePassword() || noPasswordPopup) {
            // TODO this should instead pass the primary connection information to YamcsPlugin.
            //YamcsPlugin.getDefault().connect(null);
            System.out.println("Will NOW connect to " + conf.getName() + " w password " + conf.getPassword());
        } else {
            // Authentication is needed, call the org.csstudio.security.ui login command
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
            IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
            try {
                Command cmd = commandService.getCommand("org.csstudio.security.login");
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not execute login command", e);
                //MessageDialog.openError(Display.getCurrent().getActiveShell(), "Connect",
                //      "Unable to connect to the Yamcs server.\nDetails: " + e.getMessage());
            }
        }
    }
}
