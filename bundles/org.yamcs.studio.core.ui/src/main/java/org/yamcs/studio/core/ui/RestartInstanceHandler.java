package org.yamcs.studio.core.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.ClientInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class RestartInstanceHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(RestartInstanceHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ClientInfo clientInfo = catalogue.getCurrentClientInfo();
        Shell shell = HandlerUtil.getActiveShell(event);

        String instance = clientInfo.getInstance();
        catalogue.restartInstance(clientInfo.getInstance()).whenComplete((ret, ex) -> {
            log.log(Level.SEVERE, "Failed to restart instance '" + instance + "'", ex);
            Display.getDefault().asyncExec(() -> {
                MessageDialog.openError(shell, "Failed to restart instance '" + instance + "'", ex.getMessage());
            });
        });

        return null;
    }
}
