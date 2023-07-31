package org.yamcs.studio.commanding.stack;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.commanding.stack.CommandStack.StackStatus;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.common.util.concurrent.MoreExecutors;

public class RunCommandJob extends Job {

    private static final Logger log = Logger.getLogger(RunCommandJob.class.getName());

    private Shell shell;
    private IProgressMonitor monitor;

    private CommandStackView view;
    private List<StackedCommand> commands;

    public RunCommandJob(Shell shell, CommandStackView view, List<StackedCommand> commands) {
        super("Running commands");
        this.shell = shell;
        this.view = view;
        this.commands = commands;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        this.monitor = monitor;
        var stack = CommandStack.getInstance();

        var commandCount = commands.size();

        stack.setStackStatus(StackStatus.EXECUTING);
        Display.getDefault().asyncExec(() -> view.refreshState());

        if (commandCount == 1) {
            monitor.beginTask("Running 1 command", commandCount);
        } else {
            monitor.beginTask("Running " + commandCount + " commands", commandCount);
        }

        try {
            runCommands(shell, stack);
        } catch (ExecutionException e) {
            log.log(Level.SEVERE, "Failed to run commands", e.getCause());
            MessageDialog.openError(shell, "Failed to run commands: ", e.getMessage());
        } finally {
            stack.setStackStatus(StackStatus.IDLE);
            Display.getDefault().asyncExec(() -> view.refreshState());
        }

        monitor.done();
        return Status.OK_STATUS;
    }

    private void runCommands(Shell activeShell, CommandStack stack) throws ExecutionException {
        if (monitor.isCanceled()) {
            return;
        }

        var processor = YamcsPlugin.getProcessorClient();

        for (var command : commands) {
            var qname = command.getName();

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

            try {
                var response = builder.issue(MoreExecutors.directExecutor()).get();
                command.setStackedState(StackedState.ISSUED);
                command.updateExecutionState(response);
                monitor.worked(1);
            } catch (java.util.concurrent.ExecutionException e) {
                monitor.done();
                stack.setStackStatus(StackStatus.IDLE);
                log.log(Level.SEVERE, e.getCause().getMessage(), e);
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    view.refreshState();
                });
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            var waitTime = stack.getWaitTime();
            if (command.getWaitTime() >= 0) {
                waitTime = command.getWaitTime();
            }
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
