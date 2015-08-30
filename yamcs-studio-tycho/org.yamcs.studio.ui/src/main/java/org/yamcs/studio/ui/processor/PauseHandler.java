package org.yamcs.studio.ui.processor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest.Operation;
import org.yamcs.studio.core.ManagementCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.AbstractRestHandler;

import com.google.protobuf.MessageLite;

public class PauseHandler extends AbstractRestHandler {

    private static final Logger log = Logger.getLogger(PauseHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        if (!checkRestClient(event, "pause processing"))
            return null;

        ProcessorInfo processorInfo = ManagementCatalogue.getInstance().getCurrentProcessorInfo();
        ProcessorRequest req = ProcessorRequest.newBuilder().setOperation(Operation.PAUSE).build();
        restClient.createProcessorRequest(processorInfo.getName(), req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not pause processing", e);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(HandlerUtil.getActiveShell(event), "Could not pause processing", e.getMessage());
                });
            }
        });
        return null;
    }
}
