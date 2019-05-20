package org.yamcs.studio.commanding.stack;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.commanding.stack.CommandStack.AutoMode;
import org.yamcs.studio.commanding.stack.CommandStack.StackStatus;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;

public class IssueAllCommandsHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(IssueAllCommandsHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        CommandStackView commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);

        CommandIssuer issuer = new CommandIssuer(shell, commandStackView);
        try {
            new ProgressMonitorDialog(shell).run(true, true, issuer);
        } catch (InvocationTargetException e) {
            log.severe("Automatic Command Stack cancelled");
            MessageDialog.openError(shell, "Failed to issue commands: ", e.getMessage());
        } catch (InterruptedException e) {
            log.severe("Automatic Command Stack cancelled");
            MessageDialog.openError(shell, "Failed to issue commands: ", e.getMessage());
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private static class CommandIssuer implements IRunnableWithProgress {
        private Shell shell;
        private CommandStackView commandStackView;
        private IProgressMonitor monitor;

        CommandIssuer(Shell shell, CommandStackView commandStackView) {
            this.shell = shell;
            this.commandStackView = commandStackView;
            monitor = null;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            this.monitor = monitor;
            CommandStack stack = CommandStack.getInstance();
            int startIndex = stack.getCommands().indexOf(stack.getActiveCommand());
            int nbCommands = stack.getCommands().size() - startIndex;

            stack.stackStatus = StackStatus.EXECUTING;
            log.info("Issuing the Automatic Command Stack...");
            this.monitor.beginTask("The Automatic Stack is issuing " + nbCommands + " commands", stack.getCommands().size() - startIndex);

            try {
                issueAllCommands(shell, commandStackView, stack, stack.getCommands().indexOf(stack.getActiveCommand()));
            } catch (ExecutionException e) {
                log.severe("Automatic Command Stack cancelled");
                MessageDialog.openError(shell, "Failed to issue commands: ", e.getMessage());
            }

            while (stack.stackStatus == StackStatus.EXECUTING && !monitor.isCanceled()) {
                // Todo would be nicer with Futures
                Thread.sleep(200);
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
                                    }
                                    else
                                    {
                                        // with stack delays
                                        StackedCommand nextCommand = CommandStack.getInstance().getCommands().get(commandIndex+1);
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
