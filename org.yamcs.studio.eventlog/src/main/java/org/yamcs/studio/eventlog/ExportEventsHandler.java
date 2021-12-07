package org.yamcs.studio.eventlog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.CsvWriter;

public class ExportEventsHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ExportEventsHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var part = HandlerUtil.getActivePartChecked(event);
        var eventLogView = (EventLogView) part;

        var dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.csv" });
        var targetFile = dialog.open();
        if (targetFile == null) { // cancelled
            return null;
        }

        try {
            var events = eventLogView.getEventLog().getSortedEvents();
            writeEvents(new File(targetFile), events);
            MessageDialog.openInformation(shell, "Export Events", "Events exported successfully.");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to export events: " + e.getMessage(), e);
            MessageDialog.openError(shell, "Export Events", "Unable to export events.\nDetails:" + e.getMessage());
        }

        return null;
    }

    private void writeEvents(File targetFile, List<Event> events) throws IOException {
        try (var writer = new CsvWriter(new FileWriter(targetFile))) {
            writer.writeHeader(new String[] { "Severity", "Message", "Type", "Source", "Generation", "Reception",
                    "Sequence Number" });

            for (Event event : events) {
                var formattedGenerationTime = "";
                if (event.hasGenerationTime()) {
                    var generationTime = Instant.ofEpochSecond(event.getGenerationTime().getSeconds(),
                            event.getGenerationTime().getNanos());
                    formattedGenerationTime = YamcsPlugin.getDefault().formatInstant(generationTime);
                }
                var formattedReceptionTime = "";
                if (event.hasReceptionTime()) {
                    var receptionTime = Instant.ofEpochSecond(event.getReceptionTime().getSeconds(),
                            event.getReceptionTime().getNanos());
                    formattedReceptionTime = YamcsPlugin.getDefault().formatInstant(receptionTime);
                }

                writer.writeRecord(new String[] { event.hasSeverity() ? "" + event.getSeverity() : "",
                        event.hasMessage() ? event.getMessage() : "", event.hasType() ? event.getType() : "",
                        event.hasSource() ? event.getSource() : "", formattedGenerationTime, formattedReceptionTime,
                        event.hasSeqNumber() ? "" + event.getSeqNumber() : "" });
            }
        }
    }
}
