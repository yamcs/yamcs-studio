/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.GsonBuilder;

public class ExportYcsCommandStackHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ExportYcsCommandStackHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var stack = CommandStack.getInstance();

        var commands = stack.getCommands();
        if (commands == null || commands.isEmpty()) {
            MessageDialog.openError(shell, "Export Command Stack",
                    "Command stack is empty. Nothing to export.");
            return null;
        }

        var dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.ycs" });
        var exportFile = dialog.open();
        if (exportFile == null) {
            // cancelled
            return null;
        }

        try {
            var jsonObject = ExportUtil.toJSON(stack);
            var gson = new GsonBuilder().setPrettyPrinting().create();
            var json = gson.toJson(jsonObject);
            Files.write(Paths.get(exportFile), json.getBytes(StandardCharsets.UTF_8));
            MessageDialog.openInformation(shell, "Export Command Stack", "Command stack exported successfully.");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while exporting stack", e);
            MessageDialog.openError(shell, "Export Command Stack",
                    "Unable to perform command stack export.\nDetails:" + e.getMessage());
        }

        return null;
    }
}
