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
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

/**
 * Currently only resumes a paused replay. Should eventually also seek to the
 * beginning and replay a stopped replay. We should probably do this at the
 * server level, rather than stitching it in here.
 */
public class PlayHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(PlayHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ProcessorInfo processorInfo = ManagementCatalogue.getInstance().getCurrentProcessorInfo();
        PatchProcessorRequest req = PatchProcessorRequest.newBuilder().setState("RUNNING").build();

        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        restClient.patchProcessorRequest(processorInfo.getInstance(), processorInfo.getName(), req,
                new ResponseHandler() {
                    @Override
                    public void onMessage(MessageLite responseMsg) {
                    }

                    @Override
                    public void onException(Exception e) {
                        log.log(Level.SEVERE, "Could not resume processing", e);
                        Display.getDefault().asyncExec(() -> {
                            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Could not resume processing",
                                    e.getMessage());
                        });
                    }
                });
        return null;
    }
}
