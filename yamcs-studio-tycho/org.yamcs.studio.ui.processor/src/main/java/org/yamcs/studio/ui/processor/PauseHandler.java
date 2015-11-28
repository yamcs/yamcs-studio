package org.yamcs.studio.ui.processor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.PatchProcessorRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;

import com.google.protobuf.MessageLite;

public class PauseHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(PauseHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ProcessorInfo processorInfo = catalogue.getCurrentProcessorInfo();
        PatchProcessorRequest req = PatchProcessorRequest.newBuilder().setState("PAUSED").build();
        catalogue.patchProcessorRequest(processorInfo.getName(), req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                // success
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not pause processing", e);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(HandlerUtil.getActiveShell(event), "Could not pause processing",
                            e.getMessage());
                });
            }
        });
        return null;
    }
}
