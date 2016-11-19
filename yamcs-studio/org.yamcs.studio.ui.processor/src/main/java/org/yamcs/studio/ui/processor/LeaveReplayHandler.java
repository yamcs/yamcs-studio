package org.yamcs.studio.ui.processor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Rest.EditClientRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.css.OPIUtils;

import com.google.protobuf.MessageLite;

public class LeaveReplayHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(LeaveReplayHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ClientInfo clientInfo = catalogue.getCurrentClientInfo();
        EditClientRequest req = EditClientRequest.newBuilder().setProcessor("realtime").build();
        catalogue.editClientRequest(clientInfo.getId(), req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                Display.getDefault().asyncExec(() -> {
                    OPIUtils.resetDisplays();
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not switch processor", e);
            }
        });

        return null;
    }
}
