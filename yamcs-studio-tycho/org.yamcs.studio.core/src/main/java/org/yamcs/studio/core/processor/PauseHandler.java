package org.yamcs.studio.core.processor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest.Operation;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

public class PauseHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        RestClient restClient = YamcsPlugin.getDefault().getRestClient();
        String processorName = YamcsPlugin.getDefault().getClientInfo().getProcessorName();
        ProcessorRequest req = ProcessorRequest.newBuilder().setOperation(Operation.PAUSE).build();
        restClient.createProcessorRequest(processorName, req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                System.out.println("msg " + responseMsg);
            }

            @Override
            public void onException(Exception e) {
                System.out.println("exc " + e);
            }
        });
        //IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        return null;
    }
}
