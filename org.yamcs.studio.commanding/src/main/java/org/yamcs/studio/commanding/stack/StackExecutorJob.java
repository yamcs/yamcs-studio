package org.yamcs.studio.commanding.stack;

import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.commanding.stack.CommandStack.AutoMode;
import org.yamcs.studio.commanding.stack.CommandStack.StackStatus;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

public class StackExecutorJob extends Job {

    private static final Logger log = Logger.getLogger(StackExecutorJob.class.getName());

    private Shell shell;
    private CommandStackView commandStackView;
    private IProgressMonitor monitor;

    public StackExecutorJob(Shell shell, CommandStackView commandStackView) {
        super("Executing stack");
        this.shell = shell;
        this.commandStackView = commandStackView;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        this.monitor = monitor;
        var stack = CommandStack.getInstance();
        var startIndex = stack.getCommands().indexOf(stack.getActiveCommand());
        var nbCommands = stack.getCommands().size() - startIndex;

        stack.setStackStatus(StackStatus.EXECUTING);
        log.info("Executing command stack");
        monitor.beginTask("Executing " + nbCommands + " commands",
                stack.getCommands().size() - startIndex);

        try {
            issueAllCommands(shell, commandStackView, stack, stack.getCommands().indexOf(stack.getActiveCommand()));
        } catch (ExecutionException e) {
            log.severe("Command stack failed to execute");
            MessageDialog.openError(shell, "Failed to issue commands: ", e.getMessage());
        }

        while (stack.getStackStatus() == StackStatus.EXECUTING && !monitor.isCanceled()) {
            try {
                Display.getDefault().syncExec(() -> {
                    // refresh the ui periodically, this takes too much time to
                    // do it for each command
                    commandStackView.selectActiveCommand();
                    commandStackView.refreshState();
                });
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        monitor.done();
        stack.setStackStatus(StackStatus.IDLE);
        return Status.OK_STATUS;
    }

    private void issueAllCommands(Shell activeShell, CommandStackView view, CommandStack stack, int commandIndex)
            throws ExecutionException {
        if (monitor.isCanceled()) {
            log.warning("Automatic Command Stack Canceled, at command index: " + commandIndex);
            return;
        }

        var command = CommandStack.getInstance().getCommands().get(commandIndex);
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
            builder.withArgument(argument.getName(), value);
        });

        builder.issue().whenComplete((response, exc) -> {
            if (exc == null) {
                try {
                    command.setStackedState(StackedState.ISSUED);
                    command.updateExecutionState(response);
                    monitor.worked(1);

                    if (commandIndex + 1 < CommandStack.getInstance().getCommands().size()) {
                        // Executing next command
                        if (stack.getAutoMode() == AutoMode.FIX_DELAY
                                || stack.getAutoMode() == AutoMode.STACK_DELAYS) {
                            try {
                                var delayMs = 0;
                                if (stack.getAutoMode() == AutoMode.FIX_DELAY) {
                                    // with fix delay
                                    delayMs = stack.getWaitTime();
                                } else {
                                    // with stack delays
                                    var nextCommand = CommandStack.getInstance().getCommands()
                                            .get(commandIndex + 1);
                                    delayMs = nextCommand.getWaitTime();
                                }
                                Thread.sleep(delayMs);
                            } catch (InterruptedException e) {
                                log.severe(
                                        "Automatic stack, unable to wait/sleep between commands: " + e.toString());
                                monitor.done();
                                stack.setStackStatus(StackStatus.IDLE);
                                MessageDialog.openError(activeShell, "Command Stack Error",
                                        "Automatic stack, unable to wait/sleep between commands: " + e.toString());
                            }
                        }

                        issueAllCommands(activeShell, view, stack, commandIndex + 1);
                    } else {
                        // Command stack is completed
                        log.info("Done executing Command Stack");
                        monitor.done();
                        stack.setStackStatus(StackStatus.IDLE);
                        Display.getDefault().asyncExec(() -> {
                            // refresh the ui only at the end, this takes too much time to
                            // do it for each commands
                            view.selectActiveCommand();
                            view.refreshState();
                        });
                    }

                } catch (Exception e) {
                    log.severe("Error while issuing commands in automatic mode: " + e);
                    monitor.done();
                    stack.setStackStatus(StackStatus.IDLE);
                    MessageDialog.openError(activeShell, "Command Stack Error",
                            "Error while issuing commands in automatic mode: " + e);
                    view.refreshState();
                }

            } else {
                monitor.done();
                stack.setStackStatus(StackStatus.IDLE);
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    view.refreshState();
                });
            }
        });
    }
}
