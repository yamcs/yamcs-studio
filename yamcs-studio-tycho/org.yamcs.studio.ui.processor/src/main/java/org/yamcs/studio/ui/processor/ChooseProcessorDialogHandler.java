package org.yamcs.studio.ui.processor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.PatchClientRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.ui.utils.AbstractRestHandler;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.css.OPIUtils;

import com.google.protobuf.MessageLite;

public class ChooseProcessorDialogHandler extends AbstractRestHandler {

    private static final Logger log = Logger.getLogger(ChooseProcessorDialogHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShellChecked(event);
        SwitchProcessorDialog dialog = new SwitchProcessorDialog(shell);
        if (dialog.open() == Window.OK) {
            ProcessorInfo info = dialog.getProcessorInfo();
            if (info != null) {
                ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
                ClientInfo clientInfo = catalogue.getCurrentClientInfo();
                PatchClientRequest req = PatchClientRequest.newBuilder().setProcessor(info.getName()).build();
                catalogue.patchClientRequest(clientInfo.getId(), req, new ResponseHandler() {
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
            }
        }

        return null;
    }
}
