package org.yamcs.studio.eventlog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write("Severity\tMessage\tType\tSource\tGeneration\tReception\tSequence Number\n");
            for (Event event : events) {
                writer.write("" + event.getSeverity());
                writer.write("\t");
                writer.write(event.getMessage());
                writer.write("\t");
                writer.write(event.getType());
                writer.write("\t");
                writer.write(event.getSource());
                writer.write("\t");
                writer.write(event.getGenerationTimeUTC());
                writer.write("\t");
                writer.write(event.getReceptionTimeUTC());
                writer.write("\t");
                writer.write(event.getSeqNumber());
                writer.write("\n");
            }
        }
    }

}
