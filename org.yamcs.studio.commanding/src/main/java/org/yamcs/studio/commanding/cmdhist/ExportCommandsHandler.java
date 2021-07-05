package org.yamcs.studio.commanding.cmdhist;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExportCommandsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        CommandHistoryView view = (CommandHistoryView) part;

        // Ask for file to export
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.csv" });
        String targetFile = dialog.open();
        if (targetFile == null) { // cancelled
            return null;
        }

        // Write CSV
        try {
            writeEvents(new File(targetFile), view.getTableViewer().getTable());
            MessageDialog.openInformation(shell, "Export Command History", "Command History exported successfully.");
        } catch (Exception e) {
            MessageDialog.openError(shell, "Export Command History",
                    "Unable to perform command history export.\nDetails:" + e.getMessage());
        }

        return null;
    }

    private void writeEvents(File targetFile, Table table) throws IOException {
        try (FileWriter writer = new FileWriter(targetFile)) {
            boolean first = true;
            for (TableColumn tc : table.getColumns()) {
                if (!first) {
                    writer.write("\t");
                }
                writer.write(tc.getText());
                first = false;
            }

            for (TableItem item : table.getItems()) {
                String[] rec = new String[table.getColumnCount()];
                for (int i = 0; i < table.getColumnCount(); i++) {
                    rec[i] = item.getText(i);
                }
                writer.write(String.join("\t", rec));
                writer.write("\n");
            }
        }
    }
}
