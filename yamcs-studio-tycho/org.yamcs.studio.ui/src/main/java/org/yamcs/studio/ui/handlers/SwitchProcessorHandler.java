package org.yamcs.studio.ui.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest.Operation;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.processor.SwitchProcessorDialog;

import com.google.protobuf.MessageLite;

public class SwitchProcessorHandler extends AbstractRestHandler {

    private static final Logger log = Logger.getLogger(SwitchProcessorHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!checkRestClient(event, "switch processor"))
            return null;

        Shell shell = HandlerUtil.getActiveShellChecked(event);
        SwitchProcessorDialog dialog = new SwitchProcessorDialog(shell);
        if (dialog.open() == Window.OK) {
            ProcessorInfo info = dialog.getProcessorInfo();
            if (info != null) {
                ProcessorManagementRequest req = ProcessorManagementRequest.newBuilder()
                        .setOperation(Operation.CONNECT_TO_PROCESSOR)
                        .setInstance(info.getInstance())
                        .setName(info.getName())
                        .addClientId(YamcsPlugin.getDefault().getClientInfo().getId()).build();
                restClient.createProcessorManagementRequest(req, new ResponseHandler() {
                    @Override
                    public void onMessage(MessageLite responseMsg) {
                        if (responseMsg instanceof RestExceptionMessage) {
                            log.log(Level.SEVERE, "Could not switch processor. " + responseMsg);
                        } else {
                            // Would prefer to get updates to this from the web socket client
                            Display.getCurrent().asyncExec(() -> {
                                YamcsPlugin.getDefault().refreshClientInfo();
                            });
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        log.log(Level.SEVERE, "Could not switch processor", e);
                    }
                });
            }
        }

        return null;
    }
}
