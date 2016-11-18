package org.yamcs.studio.ui.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.model.ManagementCatalogue;

/**
 * A dynamic menu for showing the processors and joining them when selected
 */
public class SwitchProcessorCompoundContributionItem extends CompoundContributionItem {

    public static final String SWITCH_PROCESSOR_COMMAND = "org.yamcs.studio.ui.processor.switch";

    private static final Logger log = Logger.getLogger(SwitchProcessorCompoundContributionItem.class.getName());

    @Override
    public IContributionItem[] getContributionItems() {
        List<IContributionItem> items = new ArrayList<>();

        ProcessorInfo realtimeProcessor = ManagementCatalogue.getInstance().getProcessorInfo("realtime");
        items.add(createProcessorItem(realtimeProcessor));
        items.add(new Separator());
        String instance = ConnectionManager.getInstance().getYamcsInstance();
        List<ProcessorInfo> processors = ManagementCatalogue.getInstance().getProcessors(instance);
        Collections.sort(processors, (p1, p2) -> p1.getName().compareTo(p2.getName()));
        ManagementCatalogue.getInstance().getProcessors().forEach(processor -> {
            if (!processor.getName().equals("realtime")) {
                CommandContributionItem item = createProcessorItem(processor);
                items.add(item);
            }
        });
        updateSelection();
        return items.toArray(new IContributionItem[0]);
    }

    private CommandContributionItem createProcessorItem(ProcessorInfo processor) {
        CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null, SWITCH_PROCESSOR_COMMAND,
                CommandContributionItem.STYLE_RADIO);

        HashMap<String, String> params = new HashMap<>();
        params.put(RadioState.PARAMETER_ID, processor.getName());

        itemParameter.label = processor.getName();
        itemParameter.parameters = params;

        return new CommandContributionItem(itemParameter);
    }

    private void updateSelection() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        ICommandService commandService = (ICommandService) workbench.getService(ICommandService.class);
        Command command = commandService.getCommand(SWITCH_PROCESSOR_COMMAND);
        try {
            ProcessorInfo currentProcessor = ManagementCatalogue.getInstance().getCurrentProcessorInfo();
            HandlerUtil.updateRadioState(command, currentProcessor.getName());
        } catch (ExecutionException e) {
            log.log(Level.SEVERE, "Could not update radio state", e);
        }
    }
}
