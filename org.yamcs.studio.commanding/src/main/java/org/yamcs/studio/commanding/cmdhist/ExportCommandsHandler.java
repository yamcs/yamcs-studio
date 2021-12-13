/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExportCommandsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        var shell = HandlerUtil.getActiveShell(event);
        var part = HandlerUtil.getActivePartChecked(event);
        var view = (CommandHistoryView) part;

        // Ask for file to export
        var dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.csv" });
        var targetFile = dialog.open();
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
        try (var writer = new FileWriter(targetFile)) {
            var first = true;
            for (var tc : table.getColumns()) {
                if (!first) {
                    writer.write("\t");
                }
                writer.write(tc.getText());
                first = false;
            }

            for (var item : table.getItems()) {
                var rec = new String[table.getColumnCount()];
                for (var i = 0; i < table.getColumnCount(); i++) {
                    rec[i] = item.getText(i);
                }
                writer.write(String.join("\t", rec));
                writer.write("\n");
            }
        }
    }
}
