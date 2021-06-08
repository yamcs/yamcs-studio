package org.yamcs.studio.eventlog;

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
import org.yamcs.client.StreamReceiver;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.YamcsPlugin;

public class ImportEventsHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportEventsHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        EventLogView view = (EventLogView) part;

        ImportPastEventsDialog dialog = new ImportPastEventsDialog(shell);
        if (dialog.open() == Window.OK) {
            try {
                Instant start = dialog.getStart();
                Instant stop = dialog.getStop();
                EventImporter importer = new EventImporter(shell, start, stop, view.getEventLog());
                new ProgressMonitorDialog(shell).run(true, true, importer);
            } catch (InvocationTargetException e) {
                MessageDialog.openError(shell, "Failed to import events", e.getMessage());
            } catch (InterruptedException e) {
                log.info("Import cancelled");
            }
        }
        return null;
    }

    private static class EventImporter implements IRunnableWithProgress {

        private Shell shell;
        private Instant start;
        private Instant stop;
        private EventLog eventLog;

        EventImporter(Shell shell, Instant start, Instant stop, EventLog eventLog) {
            this.shell = shell;
            this.start = start;
            this.stop = stop;
            this.eventLog = eventLog;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask("Importing events", IProgressMonitor.UNKNOWN);

            List<Event> newEvents = new ArrayList<>();

            ArchiveClient client = YamcsPlugin.getArchiveClient();

            BulkEventListener listener = batch -> {
                newEvents.addAll(batch);
                monitor.subTask(String.format("Fetched %,d events", newEvents.size()));
            };
            EventBatchGenerator batchGenerator = new EventBatchGenerator(listener);
            CompletableFuture<Void> future = client.streamEvents(batchGenerator, start, stop)
                    .whenComplete((data, exc) -> {
                        if (!batchGenerator.events.isEmpty()) {
                            listener.processEvents(new ArrayList<>(batchGenerator.events));
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
                        eventLog.addEvents(newEvents);
                        monitor.done();
                    });
                }
            } catch (java.util.concurrent.ExecutionException e) {
                monitor.done();
                MessageDialog.openError(shell, "Failed to import events", e.getCause().getMessage());
            }
        }
    }

    private static class EventBatchGenerator implements StreamReceiver<Event> {

        private BulkEventListener listener;
        private List<Event> events = new ArrayList<>();

        public EventBatchGenerator(BulkEventListener listener) {
            this.listener = listener;
        }

        @Override
        public void accept(Event event) {
            events.add(event);
            if (events.size() >= 500) {
                listener.processEvents(new ArrayList<>(events));
                events.clear();
            }
        }
    }

    /**
     * Reports on a batches of events. Useful for limiting GUI updates.
     */
    private static interface BulkEventListener {
        void processEvents(List<Event> events);
    }
}
