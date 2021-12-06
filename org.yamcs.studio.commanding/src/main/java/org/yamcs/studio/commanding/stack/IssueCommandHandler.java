package org.yamcs.studio.commanding.stack;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.client.Command;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.processor.ProcessorClient.CommandBuilder;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

public class IssueCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(IssueCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        CommandStackView commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);
        StackedCommand command = CommandStack.getInstance().getActiveCommand();
        issueCommand(shell, commandStackView, command);
        return null;
    }

    private void issueCommand(Shell activeShell, CommandStackView view, StackedCommand command)
            throws ExecutionException {
        String qname = command.getSelectedAlias();

        ProcessorClient processor = YamcsPlugin.getProcessorClient();
        CommandBuilder builder = processor.prepareCommand(qname)
                .withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());

        if (command.getComment() != null) {
            builder.withComment(command.getComment());
        }
        command.getExtra().forEach((option, value) -> {
            builder.withExtra(option, value);
        });
        command.getAssignments().forEach((argument, value) -> {
            if (!argument.hasInitialValue() || command.isDefaultChanged(argument)) {
                builder.withArgument(argument.getName(), value);
            }
        });

        builder.issue().whenComplete((response, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(() -> {
                    log.info("Issued " + qname);
                    command.setStackedState(StackedState.ISSUED);
                    command.updateExecutionState(response);

                    Command alreadyReceivedUpdate = view.getCommandExecution(response.getId());
                    if (alreadyReceivedUpdate != null) {
                        command.updateExecutionState(alreadyReceivedUpdate);
                    }

                    view.selectActiveCommand();
                    view.refreshState();
                });
            } else {
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    view.refreshState();
                });
            }
        });
    }
}
