package org.yamcs.studio.ui.processor;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.UIElement;
import org.yamcs.protobuf.Rest.PatchClientRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.css.OPIUtils;

import com.google.protobuf.MessageLite;

public class SwitchProcessorHandler extends AbstractHandler implements IElementUpdater {

    private static final Logger log = Logger.getLogger(SwitchProcessorHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (HandlerUtil.matchesRadioState(event))
            return null;

        String radioParameter = event.getParameter(RadioState.PARAMETER_ID);
        HandlerUtil.updateRadioState(event.getCommand(), radioParameter);

        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ProcessorInfo processorInfo = catalogue.getProcessorInfo(radioParameter);
        if (processorInfo != null) {
            ClientInfo clientInfo = catalogue.getCurrentClientInfo();
            PatchClientRequest req = PatchClientRequest.newBuilder().setProcessor(processorInfo.getName()).build();
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
        } else {
            log.warning("processor '" + radioParameter + "' not found in catalogue");
        }

        return null;
    }

    /*
     * Workaround to allow checking radio items in a dynamic contribution
     *
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=398647
     */
    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
        ICommandService service = (ICommandService) element.getServiceLocator().getService(ICommandService.class);
        String state = (String) parameters.get(RadioState.PARAMETER_ID);
        Command command = service.getCommand(SwitchProcessorCompoundContributionItem.SWITCH_PROCESSOR_COMMAND);
        State commandState = command.getState(RadioState.STATE_ID);
        if (commandState.getValue().equals(state)) {
            element.setChecked(true);
        }
    }
}
