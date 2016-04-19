package org.yamcs.studio.ui.commanding.cmdhist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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

import com.csvreader.CsvWriter;

public class ExportCommandsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        CommandHistoryView view = (CommandHistoryView) part;
        doExecute(view, shell);
        return null;
    }

    public void doExecute(CommandHistoryView commandHistoryView, Shell shell) {

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
            exportEventToCsv(exportFile, commandHistoryView.getTableViewer().getTable());
        } catch (Exception e) {
            MessageDialog.openError(shell, "Export Command History",
                    "Unable to perform command history export.\nDetails:" + e.getMessage());
            return;
        }

        MessageDialog.openInformation(shell, "Export Command History", "Command History exported successfully.");

    }

    private void exportEventToCsv(String exportFile, Table table) throws IOException {
        char fs = '\t';
        CsvWriter writer = null;
        File file = new File(exportFile);

        List<String> columnNames = new ArrayList<>();
        for (TableColumn tc : table.getColumns()) {
            columnNames.add(tc.getText());
        }

        try {
            writer = new CsvWriter(new FileOutputStream(file), fs, Charset.forName("UTF-8"));

            // write header
            writer.writeRecord(columnNames.toArray(new String[columnNames.size()]));
            writer.setForceQualifier(true);

            // write content
            for (TableItem item : table.getItems()) {
                ArrayList<String> row = new ArrayList<>();

                for (int i = 0; i < table.getColumnCount(); i++) {
                    row.add(item.getText(i));
                }
                writer.writeRecord(row.toArray(new String[row.size()]));
            }
        } finally {
            writer.close();
        }
    }

}
