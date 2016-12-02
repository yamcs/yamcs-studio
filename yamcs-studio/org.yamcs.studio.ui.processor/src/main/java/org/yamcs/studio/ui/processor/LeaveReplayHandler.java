package org.yamcs.studio.ui.processor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Rest.EditClientRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.ui.css.OPIUtils;

public class LeaveReplayHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ClientInfo clientInfo = catalogue.getCurrentClientInfo();
        EditClientRequest req = EditClientRequest.newBuilder().setProcessor("realtime").build();
        catalogue.editClientRequest(clientInfo.getId(), req).thenRun(() -> {
            Display.getDefault().asyncExec(() -> {
                OPIUtils.resetDisplays();
            });
        });

        return null;
    }
}
