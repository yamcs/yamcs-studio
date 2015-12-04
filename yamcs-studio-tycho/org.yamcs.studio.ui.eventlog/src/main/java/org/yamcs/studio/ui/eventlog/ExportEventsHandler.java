package org.yamcs.studio.ui.eventlog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

public class ExportEventsHandler extends AbstractHandler {

    EventLogView eventLogView = null;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // get related event view
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        this.eventLogView = (EventLogView) part;

        // Ask for file to export
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.csv" });
        String exportFile = dialog.open();
        System.out.println("export file choosen: " + exportFile);
        if (exportFile == null) {
            // cancelled
            return null;
        }

        // Write CSV
        try
        {
            exportEventToCsv(exportFile, eventLogView.getTableContentProvider());
        } catch (Exception e)
        {
            MessageDialog.openError(shell, "Export Events",
                    "Unable to perform events export.\nDetails:" + e.getMessage());
            return null;
        }

        MessageDialog.openInformation(shell, "Export Events", "Events exported successfully.");

        return null;
    }

    private void exportEventToCsv(String exportFile, EventLogContentProvider eventLogContentProvider) throws IOException {

        String fs = ","; //csvFiedSeparator
        String fd = "\""; // csvFieldDelimiter
        String eol = "\r\n"; // csvEndOfLine
        String escapedFd = "'";
        String csvHeader = "";
        String csvBody = "";

        // Build header
        csvHeader += fd + "Sequence Number" + fd + fs;
        csvHeader += fd + "Severity" + fd + fs;
        csvHeader += fd + "Message" + fd + fs;
        csvHeader += fd + "Source" + fd + fs;
        csvHeader += fd + "Type" + fd + fs;
        csvHeader += fd + "Reception Time" + fd + fs;
        csvHeader += fd + "Generation Time" + fd;
        csvHeader += eol;

        Object[] events = (Object[]) eventLogContentProvider.getElements(null);
        for (Object o : events)
        {
            Event event = (Event) o;
            csvBody += fd + event.getSeqNumber() + fd + fs;
            csvBody += fd + event.getSeverity().name() + fd + fs;
            csvBody += fd + event.getMessage().replace(fd, escapedFd) + fd + fs;
            csvBody += fd + event.getSource() + fd + fs;
            if (event.getType() != null)
                csvBody += fd + event.getType() + fd + fs;
            else
                csvBody += fd + fd + fs;
            csvBody += fd + TimeEncoding.toString((event).getReceptionTime()) + fd + fs;
            csvBody += fd + TimeEncoding.toString((event).getGenerationTime()) + fd;
            csvBody += eol;
        }
        String csv = csvHeader + csvBody;

        // write the file
        File file = new File(exportFile);
        FileWriter writer = new FileWriter(file);
        writer.write(csv);
        writer.flush();
        writer.close();
    }
}
