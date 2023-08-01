/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class ImportCommandStackHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportCommandStackHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.xml;*.ycs" });
        var importFile = dialog.open();
        if (importFile == null) {
            return null;
        }
        log.info("Importing command stack from file: " + importFile);

        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var part = window.getActivePage().findView(CommandStackView.ID);
        var commandStackView = (CommandStackView) part;

        try {
            var stack = CommandStackParser.parse(Path.of(importFile));
            commandStackView.setWaitTime(stack.getWaitTime());
            for (var sc : stack.getCommands()) {
                commandStackView.addTelecommand(sc);
            }
        } catch (CommandStackParseException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            MessageDialog.openError(shell, "Parse Command Stack",
                    "Unable to parse command stack. Details:\n" + e);
        }

        return null;
    }
}
