package org.yamcs.studio.ui.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest.Operation;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

public class GoToEndHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(GoToEndHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        RestClient restClient = YamcsPlugin.getDefault().getRestClient();
        String processorName = YamcsPlugin.getDefault().getClientInfo().getProcessorName();
        ProcessorInfo processorInfo = YamcsPlugin.getDefault().getProcessorInfo(processorName);
        long seekTime = processorInfo.getReplayRequest().getStop();

        ProcessorRequest req = ProcessorRequest.newBuilder().setOperation(Operation.SEEK).setSeekTime(seekTime).build();
        restClient.createProcessorRequest(processorName, req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                if (responseMsg instanceof RestExceptionMessage) {
                    log.severe("Could not go to end of replay range: " + responseMsg);
                    MessageDialog.openError(HandlerUtil.getActiveShell(event), "Seek Error",
                            ((RestExceptionMessage) responseMsg).getMsg());
                }
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not go to end of replay range", e);
            }
        });
        return null;
    }
}
