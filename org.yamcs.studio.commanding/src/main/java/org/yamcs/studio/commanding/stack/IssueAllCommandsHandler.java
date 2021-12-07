package org.yamcs.studio.commanding.stack;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryView;
import org.yamcs.studio.commanding.stack.CommandStack.AutoMode;
import org.yamcs.studio.commanding.stack.CommandStack.StackStatus;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

public class IssueAllCommandsHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(IssueAllCommandsHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);
        var commandHistoryView = (CommandHistoryView) window.getActivePage().findView(CommandHistoryView.ID);

        // lock scroll during command stack execution, or this would slow down the UI
        // refresh too much
        var service = PlatformUI.getWorkbench().getService(ICommandService.class);
        var command = service.getCommand("org.yamcs.studio.commanding.cmdhist.scrollLockCommand");
        var oldState = HandlerUtil.toggleCommandState(command);
        if (commandHistoryView != null) {
            commandHistoryView.enableScrollLock(true);
        }

        try {
            var issuer = new CommandIssuer(shell, commandStackView);
            new ProgressMonitorDialog(shell).run(true, true, issuer);
        } catch (Exception e) {
            log.severe("Automatic Command Stack error:" + e.getMessage());
            MessageDialog.openError(shell, "Failed to issue commands: ", e.getMessage());
        }

        log.info("Issue all commands execute done");

        // restore scroll state of the command history view
        commandHistoryView.enableScrollLock(oldState);

        return null;
    }

    private static class CommandIssuer implements IRunnableWithProgress {
        // private static class CommandIssuer implements Runnable {
        private Shell shell;
        private CommandStackView commandStackView;
        private IProgressMonitor monitor;

        CommandIssuer(Shell shell, CommandStackView commandStackView) {
            this.shell = shell;
            this.commandStackView = commandStackView;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            this.monitor = monitor;
            var stack = CommandStack.getInstance();
            var startIndex = stack.getCommands().indexOf(stack.getActiveCommand());
            var nbCommands = stack.getCommands().size() - startIndex;

            stack.stackStatus = StackStatus.EXECUTING;
            log.info("Issuing the Automatic Command Stack...");
            this.monitor.beginTask("The Automatic Stack is issuing " + nbCommands + " commands",
                    stack.getCommands().size() - startIndex);

            try {
                issueAllCommands(shell, commandStackView, stack, stack.getCommands().indexOf(stack.getActiveCommand()));
            } catch (ExecutionException e) {
                log.severe("Automatic Command Stack cancelled");
                MessageDialog.openError(shell, "Failed to issue commands: ", e.getMessage());
            }

            while (stack.stackStatus == StackStatus.EXECUTING && !monitor.isCanceled()) {
                // Todo would be nicer with Futures
                try {
                    Display.getDefault().asyncExec(() -> {
                        // refresh the ui periodically, this takes too much time to
                        // do it for each commands
                        commandStackView.selectActiveCommand();
                        commandStackView.refreshState();
                    });
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            monitor.done();
            stack.stackStatus = StackStatus.IDLE;

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
                            if (stack.autoMode == AutoMode.FIX_DELAY || stack.autoMode == AutoMode.STACK_DELAYS) {
                                try {
                                    var delayMs = 0;
                                    if (stack.autoMode == AutoMode.FIX_DELAY) {
                                        // with fix delay
                                        delayMs = stack.fixDelayMs;
                                    } else {
                                        // with stack delays
                                        var nextCommand = CommandStack.getInstance().getCommands()
                                                .get(commandIndex + 1);
                                        delayMs = nextCommand.getDelayMs();
                                    }
                                    Thread.sleep(delayMs);
                                } catch (InterruptedException e) {
                                    log.severe(
                                            "Automatic stack, unable to wait/sleep between commands: " + e.toString());
                                    monitor.done();
                                    stack.stackStatus = StackStatus.IDLE;
                                    MessageDialog.openError(activeShell, "Command Stack Error",
                                            "Automatic stack, unable to wait/sleep between commands: " + e.toString());
                                }
                            }

                            issueAllCommands(activeShell, view, stack, commandIndex + 1);
                        } else {
                            // Command stack is completed
                            log.info("Automatic Command Stack is fully issued");
                            monitor.done();
                            stack.stackStatus = StackStatus.IDLE;
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
                        stack.stackStatus = StackStatus.IDLE;
                        MessageDialog.openError(activeShell, "Command Stack Error",
                                "Error while issuing commands in automatic mode: " + e);
                        view.refreshState();
                    }

                } else {
                    monitor.done();
                    stack.stackStatus = StackStatus.IDLE;
                    Display.getDefault().asyncExec(() -> {
                        command.setStackedState(StackedState.REJECTED);
                        view.refreshState();
                    });
                }
            });
        }
    }

}
