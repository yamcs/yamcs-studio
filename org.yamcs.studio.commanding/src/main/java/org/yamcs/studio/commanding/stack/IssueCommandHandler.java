package org.yamcs.studio.commanding.stack;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

public class IssueCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(IssueCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);
        var command = CommandStack.getInstance().getActiveCommand();
        issueCommand(shell, commandStackView, command);
        return null;
    }

    private void issueCommand(Shell activeShell, CommandStackView view, StackedCommand command)
            throws ExecutionException {
        var qname = command.getName();

        var processor = YamcsPlugin.getProcessorClient();
        var builder = processor.prepareCommand(qname).withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());

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

                    var alreadyReceivedUpdate = view.getCommandExecution(response.getId());
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
