/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.processor;

import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.UIElement;
import org.yamcs.studio.core.RemoteEntityHolder;
import org.yamcs.studio.core.YamcsPlugin;

public class SwitchProcessorHandler extends AbstractHandler implements IElementUpdater {

    private static final Logger log = Logger.getLogger(SwitchProcessorHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (HandlerUtil.matchesRadioState(event)) {
            return null;
        }

        var radioParameter = event.getParameter(RadioState.PARAMETER_ID); // processor name
        HandlerUtil.updateRadioState(event.getCommand(), radioParameter);

        var yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.createProcessorClient(YamcsPlugin.getInstance(), radioParameter).getInfo()
                .whenComplete((processor, err) -> {
                    if (err == null) {
                        var holder = new RemoteEntityHolder();
                        holder.yamcsClient = YamcsPlugin.getYamcsClient();
                        holder.serverInfo = YamcsPlugin.getServerInfo();
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
        var service = element.getServiceLocator().getService(ICommandService.class);
        var state = (String) parameters.get(RadioState.PARAMETER_ID);
        var command = service.getCommand(SwitchProcessorCompoundContributionItem.SWITCH_PROCESSOR_COMMAND);
        var commandState = command.getState(RadioState.STATE_ID);
        if (commandState.getValue().equals(state)) {
            element.setChecked(true);
        }
    }
}
