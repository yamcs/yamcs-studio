package org.yamcs.studio.ui.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest.Operation;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;

import com.google.protobuf.MessageLite;

/**
 * Currently only resumes a paused replay. Should eventually also seek to the beginning and replay a
 * stopped replay. We should probably do this at the server level, rather than stitching it in here.
 */
public class PlayHandler extends AbstractRestHandler {

    private static final Logger log = Logger.getLogger(PlayHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!checkRestClient(event, "resume processing"))
            return null;

        String processorName = YamcsPlugin.getDefault().getClientInfo().getProcessorName();
        ProcessorRequest req = ProcessorRequest.newBuilder().setOperation(Operation.RESUME).build();
        restClient.createProcessorRequest(processorName, req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                if (responseMsg instanceof RestExceptionMessage) {
                    log.severe("Could not resume processing: " + responseMsg);
                    MessageDialog.openError(HandlerUtil.getActiveShell(event), "Could not resume processing",
                            ((RestExceptionMessage) responseMsg).getMsg());
                }
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not resume processing", e);
            }
        });
        return null;
    }
}
