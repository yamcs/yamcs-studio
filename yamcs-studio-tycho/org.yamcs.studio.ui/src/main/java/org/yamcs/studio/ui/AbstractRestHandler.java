package org.yamcs.studio.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.web.RestClient;

public abstract class AbstractRestHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(AbstractRestHandler.class.getName());

    protected RestClient checkRestClient(Shell shell, String action) {
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient == null) {
            String error = "Could not " + action + ", client disconnected from Yamcs server";
            MessageDialog.openError(shell, "Error", error);
            log.log(Level.SEVERE, error);
            return null;
        }
        return restClient;
    }
}
