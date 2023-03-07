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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.client.storage.ObjectId;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.GsonBuilder;

public class SaveStackToYamcsHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(SaveStackToYamcsHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var stack = CommandStack.getInstance();

        Collection<StackedCommand> commands = stack.getCommands();
        if (commands == null || commands.isEmpty()) {
            MessageDialog.openError(shell, "Save Command Stack",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        var dialog = new SaveStackToYamcsDialog(shell);
        if (dialog.open() == Window.OK) {
            if (dialog.xmlFormat) {
                return saveXML(shell, stack, dialog.finalObjectName);
            } else {
                System.out.println("saving ycs");
                return saveYCS(shell, stack, dialog.finalObjectName);
            }
        }

        return null;
    }

    private Object saveXML(Shell shell, CommandStack stack, String objectName) throws ExecutionException {
        String xml;
        try {
            xml = ExportUtil.toXML(stack);
        } catch (IOException | TransformerException e) {
            log.log(Level.SEVERE, "Error while exporting stack", e);
            MessageDialog.openError(shell, "Export Command Stack",
                    "Unable to perform command stack export.\nDetails:" + e.getMessage());
            return null;
        }

        var storage = YamcsPlugin.getStorageClient();
        var id = ObjectId.of("stacks", objectName);
        storage.uploadObject(id, xml.getBytes(StandardCharsets.UTF_8)).exceptionally(err -> {
            MessageDialog.openError(shell, "Save Command Stack",
                    "Unable to save stack.\nDetails:" + err.getMessage());
            return null;
        });

        return null;
    }

    private Object saveYCS(Shell shell, CommandStack stack, String objectName) throws ExecutionException {
        var jsonObject = ExportUtil.toJSON(stack);
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var json = gson.toJson(jsonObject);

        var storage = YamcsPlugin.getStorageClient();
        var id = ObjectId.of("stacks", objectName);
        storage.uploadObject(id, json.getBytes(StandardCharsets.UTF_8)).exceptionally(err -> {
            MessageDialog.openError(shell, "Save Command Stack",
                    "Unable to save stack.\nDetails:" + err.getMessage());
            return null;
        });

        return null;
    }

    private static class SaveStackToYamcsDialog extends Dialog {

        private Text nameText;
        private Combo formatCombo;
        private Text pathText;

        String finalObjectName;
        boolean xmlFormat;

        protected SaveStackToYamcsDialog(Shell parentShell) {
            super(parentShell);
        }

        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText("Save to Yamcs");
        }

        @Override
        protected Point getInitialSize() {
            return new Point(350, 300);
        }

        @Override
        protected boolean isResizable() {
            return true;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            var container = (Composite) super.createDialogArea(parent);

            var label = new Label(container, SWT.NONE);
            label.setText("Stack name:");
            nameText = new Text(container, SWT.BORDER);
            nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            nameText.addListener(SWT.Modify, evt -> updateState());

            label = new Label(container, SWT.NONE);
            label.setText("Format:");
            formatCombo = new Combo(container, SWT.BORDER);
            formatCombo.setItems("XML (deprecated)", "YCS (recommended)");
            formatCombo.select(1);
            formatCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            label = new Label(container, SWT.NONE);
            label.setText("Path:");
            pathText = new Text(container, SWT.BORDER);
            pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            pathText.setText("/");
            pathText.addListener(SWT.Modify, evt -> updateState());

            return container;
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);
            updateState();
        }

        @Override
        protected void okPressed() {
            xmlFormat = formatCombo.getSelectionIndex() == 0;
            var name = nameText.getText().trim() + (xmlFormat ? ".xml" : ".ycs");
            var path = pathText.getText().trim();

            finalObjectName = path;
            if ("/".equals(path)) {
                finalObjectName = "";
            } else if (path.startsWith("/")) {
                finalObjectName = path.substring(1);
            }

            if (finalObjectName.endsWith("/")) {
                finalObjectName = finalObjectName.substring(0, finalObjectName.length() - 1);
            }

            if (finalObjectName.isEmpty()) {
                finalObjectName = name;
            } else {
                finalObjectName += "/" + name;
            }

            super.okPressed();
        }

        private void updateState() {
            var okButton = getButton(IDialogConstants.OK_ID);
            var isValid = true;

            var name = nameText.getText();
            if (name == null || name.trim().isEmpty() || name.contains("/")) {
                isValid = false;
            }

            var path = pathText.getText();
            if (path == null || path.trim().isEmpty() || !path.trim().startsWith("/")) {
                isValid = false;
            }

            okButton.setEnabled(isValid);
        }
    }
}
