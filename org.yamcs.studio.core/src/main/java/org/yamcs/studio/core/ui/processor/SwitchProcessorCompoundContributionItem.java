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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * A dynamic menu for showing the processors and joining them when selected
 */
public class SwitchProcessorCompoundContributionItem extends CompoundContributionItem {

    public static final String SWITCH_PROCESSOR_COMMAND = "org.yamcs.studio.core.ui.processor.switch";

    private static final Logger log = Logger.getLogger(SwitchProcessorCompoundContributionItem.class.getName());

    @Override
    public IContributionItem[] getContributionItems() {
        List<IContributionItem> items = new ArrayList<>();

        var instance = YamcsPlugin.getInstance();
        var currentProcessor = YamcsPlugin.getProcessor();
        if (currentProcessor != null) {
            items.add(createProcessorItem(currentProcessor));
            items.add(new Separator());
        }

        try {
            var client = YamcsPlugin.getYamcsClient();
            var processors = client.listProcessors(instance).get(3000, TimeUnit.MILLISECONDS);
            processors.stream().filter(p -> instance.equals(p.getInstance())).forEach(processor -> {
                if (currentProcessor != null && !processor.getName().equals(currentProcessor)) {
                    var item = createProcessorItem(processor.getName());
                    items.add(item);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // Ignore
        } catch (java.util.concurrent.ExecutionException e) {
        }
        updateSelection();

        return items.toArray(new IContributionItem[0]);
    }

    private CommandContributionItem createProcessorItem(String processor) {
        var itemParameter = new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                null, SWITCH_PROCESSOR_COMMAND, CommandContributionItem.STYLE_RADIO);

        var params = new HashMap<String, String>();
        params.put(RadioState.PARAMETER_ID, processor);

        itemParameter.label = processor;
        itemParameter.parameters = params;

        return new CommandContributionItem(itemParameter);
    }

    private void updateSelection() {
        var workbench = PlatformUI.getWorkbench();
        var commandService = workbench.getService(ICommandService.class);
        var command = commandService.getCommand(SWITCH_PROCESSOR_COMMAND);
        try {
            var currentProcessor = YamcsPlugin.getProcessor();
            HandlerUtil.updateRadioState(command, currentProcessor);
        } catch (ExecutionException e) {
            log.log(Level.SEVERE, "Could not update radio state", e);
        }
    }
}
