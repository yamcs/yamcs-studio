package org.yamcs.studio.commanding.cmdhist;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.client.Command;
import org.yamcs.client.StreamReceiver;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.studio.core.YamcsPlugin;

public class ImportCommandsHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportCommandsHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        CommandHistoryView view = (CommandHistoryView) part;
        ImportPastCommandsDialog dialog = new ImportPastCommandsDialog(shell);
        if (dialog.open() == Window.OK) {
            try {
                Instant start = dialog.getStart();
                Instant stop = dialog.getStop();
                CommandImporter importer = new CommandImporter(shell, start, stop, view);
                new ProgressMonitorDialog(shell).run(true, true, importer);
            } catch (InvocationTargetException e) {
                MessageDialog.openError(shell, "Failed to import events", e.getMessage());
            } catch (InterruptedException e) {
                log.info("Import cancelled");
            }
        }
        return null;
    }

    private static class CommandImporter implements IRunnableWithProgress {

        private Shell shell;
        private Instant start;
        private Instant stop;
        private CommandHistoryView commandHistoryView;

        CommandImporter(Shell shell, Instant start, Instant stop, CommandHistoryView commandHistoryView) {
            this.shell = shell;
            this.start = start;
            this.stop = stop;
            this.commandHistoryView = commandHistoryView;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask("Importing commands", IProgressMonitor.UNKNOWN);

            List<Command> newCommands = new ArrayList<>();

            ArchiveClient client = YamcsPlugin.getArchiveClient();

            BulkCommandListener listener = batch -> {
                newCommands.addAll(batch);
                monitor.subTask(String.format("Fetched %,d commands", newCommands.size()));
            };
            CommandBatchGenerator batchGenerator = new CommandBatchGenerator(listener);
            CompletableFuture<Void> future = client.streamCommands(batchGenerator, start, stop)
                    .whenComplete((data, exc) -> {
                        if (!batchGenerator.commands.isEmpty()) {
                            listener.processCommands(new ArrayList<>(batchGenerator.commands));
                        }
                    });

            try {
                while (!monitor.isCanceled() && !future.isDone()) {
                    try {
                        future.get(200, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        // Keep trying until cancelled or done
                    }
                }

                if (monitor.isCanceled()) {
                    future.cancel(true);
                    throw new InterruptedException();
                } else {
                    monitor.subTask("Updating table");
                    Display.getDefault().syncExec(() -> {
                        commandHistoryView.addCommands(newCommands);
                        monitor.done();
                    });
                }
            } catch (java.util.concurrent.ExecutionException e) {
                monitor.done();
                MessageDialog.openError(shell, "Failed to import commands", e.getCause().getMessage());
            }
        }
    }

    private static class CommandBatchGenerator implements StreamReceiver<Command> {

        private BulkCommandListener listener;
        private List<Command> commands = new ArrayList<>();

        public CommandBatchGenerator(BulkCommandListener listener) {
            this.listener = listener;
        }

        @Override
        public void accept(Command command) {
            commands.add(command);
            if (commands.size() >= 500) {
                listener.processCommands(new ArrayList<>(commands));
                commands.clear();
            }
        }
    }

    /**
     * Reports on batches of commands. Useful for limiting GUI updates.
     */
    private static interface BulkCommandListener {
        void processCommands(List<Command> commands);
    }
}
