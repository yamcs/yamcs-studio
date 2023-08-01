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
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * General-purpose job to run multiple commands.
 * <p>
 * This does not check 'armed' state. We don't always want that.
 */
public class RunCommandJob extends Job {

    private static final Logger log = Logger.getLogger(RunCommandJob.class.getName());

    private Shell shell;
    private IProgressMonitor monitor;

    private CommandStack stack;
    private List<StackedCommand> commands;

    // Only set if running the 'global' stack.
    // In the future, could have multiple views.
    private CommandStackView view;

    public RunCommandJob(Shell shell, CommandStack stack, List<StackedCommand> commands, CommandStackView view) {
        super("Running commands");
        this.shell = shell;
        this.stack = stack;
        this.commands = commands;
        this.view = view;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        this.monitor = monitor;

        var commandCount = commands.size();

        stack.setExecuting(true);
        if (view != null) {
            Display.getDefault().asyncExec(() -> view.refreshState());
        }

        if (commandCount == 1) {
            monitor.beginTask("Running 1 command", commandCount);
        } else {
            monitor.beginTask("Running " + commandCount + " commands", commandCount);
        }

        try {
            runCommands(stack);
        } catch (ExecutionException e) {
            log.log(Level.SEVERE, "Failed to run commands", e);
            MessageDialog.openError(shell, "Failed to run commands: ", e.getMessage());
        } finally {
            stack.setExecuting(false);
            if (view != null) {
                Display.getDefault().asyncExec(() -> view.refreshState());
            }
        }

        monitor.done();
        return Status.OK_STATUS;
    }

    private void runCommands(CommandStack stack) throws ExecutionException {
        if (monitor.isCanceled()) {
            return;
        }

        var processor = YamcsPlugin.getProcessorClient();

        var commandIndex = 0;
        try {
            for (; commandIndex < commands.size(); commandIndex++) {
                var command = commands.get(commandIndex);
                var qname = command.getName();

                var builder = processor.prepareCommand(qname)
                        .withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
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
                    stack.setExecuting(false);
                    log.log(Level.SEVERE, e.getCause().getMessage(), e.getCause());
                    command.setStackedState(StackedState.REJECTED);
                    if (view != null) {
                        Display.getDefault().asyncExec(() -> view.refreshState());
                    }
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
        } finally {
            if (view != null) {
                if (commandIndex < commands.size()) {
                    // Not the last command, something must have happened.
                    // So keep the selection where it is.
                } else {
                    var currentCommand = commands.get(commandIndex - 1);
                    Display.getDefault().asyncExec(() -> {
                        view.selectNextCommand(currentCommand);
                    });
                }
            }
        }
    }
}
