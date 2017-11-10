package org.yamcs.studio.core.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class RestartInstanceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ClientInfo clientInfo = catalogue.getCurrentClientInfo();
        catalogue.restartInstance(clientInfo.getInstance());

        return null;
    }
}
