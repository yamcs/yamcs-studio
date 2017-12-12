package org.yamcs.studio.eventlog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

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

        // get related event view
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        EventLogView eventLogView = (EventLogView) part;
        doExecute(eventLogView, shell);
        return null;
    }

    public void doExecute(EventLogView eventLogView, Shell shell) {

        // Ask for file to export
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.csv" });
        String exportFile = dialog.open();
        System.out.println("export file choosen: " + exportFile);
        if (exportFile == null) {
            // cancelled
            return;
        }

        // Write CSV
        try {
            exportEventToCsv(exportFile, eventLogView.getTableContentProvider());
        } catch (Exception e) {
            MessageDialog.openError(shell, "Export Events",
                    "Unable to perform events export.\nDetails:" + e.getMessage());
            return;
        }

        MessageDialog.openInformation(shell, "Export Events", "Events exported successfully.");

    }

    private void exportEventToCsv(String exportFile, EventLogContentProvider eventLogContentProvider) throws IOException {
        char fs = '\t';
        CsvWriter writer = null;
        File file = new File(exportFile);
        try {
            writer = new CsvWriter(new FileOutputStream(file), fs, Charset.forName("UTF-8"));
            writer.writeRecord(new String[] { "Sequence Number", "Severity", "Message", "Source", "Type", "Reception Time", "Generation Time" });
            writer.setForceQualifier(true);
            for (Event event : eventLogContentProvider.getSortedEvents()) {

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
