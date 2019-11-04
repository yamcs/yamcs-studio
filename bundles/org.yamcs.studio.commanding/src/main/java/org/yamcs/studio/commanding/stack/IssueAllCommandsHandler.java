package org.yamcs.studio.commanding.stack;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.IssueCommandRequest;
import org.yamcs.protobuf.IssueCommandResponse;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryView;
import org.yamcs.studio.commanding.stack.CommandStack.AutoMode;
import org.yamcs.studio.commanding.stack.CommandStack.StackStatus;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.model.CommandingCatalogue;

import com.google.protobuf.InvalidProtocolBufferException;

public class IssueAllCommandsHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(IssueAllCommandsHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        CommandStackView commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);
        CommandHistoryView commandHistoryView = (CommandHistoryView) window.getActivePage()
                .findView(CommandHistoryView.ID);

        // lock scroll during command stack execution, or this would slow down the UI
        // refresh too much
        ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        Command command = service.getCommand("org.yamcs.studio.commanding.cmdhist.scrollLockCommand");
        boolean oldState = HandlerUtil.toggleCommandState(command);
        if (commandHistoryView != null) {
            commandHistoryView.enableScrollLock(true);
        }

        try {
            CommandIssuer issuer = new CommandIssuer(shell, commandStackView);
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
            CommandStack stack = CommandStack.getInstance();
            int startIndex = stack.getCommands().indexOf(stack.getActiveCommand());
            int nbCommands = stack.getCommands().size() - startIndex;

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

            StackedCommand command = CommandStack.getInstance().getCommands().get(commandIndex);
            IssueCommandRequest req = command.toIssueCommandRequest().build();
            CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
            String qname;
            try {
                qname = command.getSelectedAliasEncoded();
            } catch (UnsupportedEncodingException e1) {
                throw new ExecutionException(e1.getMessage());
            }

            catalogue.sendCommand("realtime", qname, req).whenComplete((data, exc) -> {
                if (exc == null) {
                    try {
                        IssueCommandResponse response = IssueCommandResponse.newBuilder()
                                .mergeFrom(data)
                                .build();
                        command.setCommandId(response.getId());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        command.setStackedState(StackedState.ISSUED);
                        monitor.worked(1);

                        if (commandIndex + 1 < CommandStack.getInstance().getCommands().size()) {
                            // Executing next command
                            if (stack.autoMode == AutoMode.FIX_DELAY || stack.autoMode == AutoMode.STACK_DELAYS) {
                                try {
                                    int delayMs = 0;
                                    if (stack.autoMode == AutoMode.FIX_DELAY) {
                                        // with fix delay
                                        delayMs = stack.fixDelayMs;
                                    } else {
                                        // with stack delays
                                        StackedCommand nextCommand = CommandStack.getInstance().getCommands()
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
