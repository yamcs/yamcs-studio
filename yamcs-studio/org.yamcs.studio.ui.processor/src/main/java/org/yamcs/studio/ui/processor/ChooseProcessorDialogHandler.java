package org.yamcs.studio.ui.processor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.EditClientRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;

import com.google.protobuf.MessageLite;

public class ChooseProcessorDialogHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ChooseProcessorDialogHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShellChecked(event);
        SwitchProcessorDialog dialog = new SwitchProcessorDialog(shell);
        if (dialog.open() == Window.OK) {
            ProcessorInfo info = dialog.getProcessorInfo();
            if (info != null) {
                ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
                int currentClientId = catalogue.getCurrentClientInfo().getId();
                EditClientRequest req = EditClientRequest.newBuilder().setInstance(info.getInstance())
                        .setProcessor(info.getName()).build();

                // Internal state will be changed automatically within
                // the ManagementCatalogue.
                catalogue.editClientRequest(currentClientId, req, new ResponseHandler() {
                    @Override
                    public void onMessage(MessageLite responseMsg) {
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
