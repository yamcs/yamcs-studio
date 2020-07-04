package org.yamcs.studio.core.ui.processor;

import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.UIElement;
import org.yamcs.client.YamcsClient;
import org.yamcs.studio.core.RemoteEntityHolder;
import org.yamcs.studio.core.YamcsPlugin;

public class SwitchProcessorHandler extends AbstractHandler implements IElementUpdater {

    private static final Logger log = Logger.getLogger(SwitchProcessorHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (HandlerUtil.matchesRadioState(event)) {
            return null;
        }

        String radioParameter = event.getParameter(RadioState.PARAMETER_ID); // processor name
        HandlerUtil.updateRadioState(event.getCommand(), radioParameter);

        YamcsClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.createProcessorClient(YamcsPlugin.getInstance(), radioParameter).getInfo()
                .whenComplete((processor, err) -> {
                    if (err == null) {
                        RemoteEntityHolder holder = new RemoteEntityHolder();
                        holder.yamcsClient = YamcsPlugin.getYamcsClient();
                        holder.userInfo = YamcsPlugin.getUser();
                        holder.missionDatabase = YamcsPlugin.getMissionDatabase();
                        holder.instance = YamcsPlugin.getInstance();
                        holder.processor = processor;
                        log.info(String.format("Switching to '%s' processor (instance: %s)", processor.getName(),
                                processor.getInstance()));
                        YamcsPlugin.updateEntities(holder);
                    }
                });

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
