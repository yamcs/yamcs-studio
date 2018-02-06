package org.yamcs.studio.eventlog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.utils.TimeEncoding;

import com.csvreader.CsvWriter;

public class ExportEventsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        EventLogView eventLogView = (EventLogView) part;

        // Ask for file to export
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.csv" });
        String targetFile = dialog.open();
        if (targetFile == null) { // cancelled
            return null;
        }

        // Write CSV
        try {
            List<Event> events = eventLogView.getEventLog().getEvents();
            writeEvents(new File(targetFile), events);
            MessageDialog.openInformation(shell, "Export Events", "Events exported successfully.");
        } catch (Exception e) {
            MessageDialog.openError(shell, "Export Events",
                    "Unable to perform events export.\nDetails:" + e.getMessage());
        }

        return null;
    }

    private void writeEvents(File targetFile, List<Event> events) throws IOException {
        CsvWriter writer = null;
        try {
            writer = new CsvWriter(new FileOutputStream(targetFile), '\t', Charset.forName("UTF-8"));
            writer.writeRecord(new String[] { "Sequence Number", "Severity", "Message", "Source", "Type",
                    "Reception Time", "Generation Time" });
            writer.setForceQualifier(true);
            for (Event event : events) {
                writer.writeRecord(new String[] {
                        event.getSeqNumber() + "",
                        event.getSeverity().name(),
                        event.getMessage(),
                        event.getSource() != null ? event.getSource() : "",
                        event.getType() != null ? event.getType() : "",
                        TimeEncoding.toString((event).getReceptionTime()),
                        TimeEncoding.toString((event).getGenerationTime()) });
            }
        } finally {
            writer.close();
        }
    }

}
