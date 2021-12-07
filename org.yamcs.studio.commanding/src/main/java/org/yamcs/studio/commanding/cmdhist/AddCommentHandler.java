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

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.studio.core.YamcsPlugin;

public class AddCommentHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        var shell = HandlerUtil.getActiveShell(event);
        if (sel != null && sel instanceof IStructuredSelection) {
            var selection = (IStructuredSelection) sel;
            if (selection.isEmpty()) {
                return null;
            }

            // Populate with text of first record. But only if single selection
            var initialValue = "";
            if (selection.size() == 1) {
                var rec = (CommandHistoryRecord) selection.getFirstElement();
                var existingComment = rec.getTextForColumn("Comment", false);
                if (existingComment != null) {
                    initialValue = existingComment;
                }
            }

            String dialogMessage;
            if (selection.size() == 1) {
                dialogMessage = "Add a comment for this command";
            } else {
                dialogMessage = "Add a comment for the " + selection.size() + " selected commands";
            }

            IInputValidator validator = newText -> null;
            InputDialog commentDialog = new InputDialog(shell, "Add Comment", dialogMessage, initialValue, validator) {

                @Override
                protected int getInputTextStyle() {
                    return SWT.MULTI | SWT.BORDER | SWT.V_SCROLL;
                }

                @Override
                protected Control createDialogArea(Composite parent) {
                    var res = super.createDialogArea(parent);
                    ((GridData) this.getText().getLayoutData()).heightHint = 4 * this.getText().getLineHeight();
                    return res;
                }
            };

            var commentResult = commentDialog.open();
            if (commentResult == Window.OK) {
                var newComment = commentDialog.getValue();

                // TODO improve me. Bundle into only one error message
                // Or even better: one api call.
                Iterator<?> it = selection.iterator();
                while (it.hasNext()) {
                    var rec = (CommandHistoryRecord) it.next();
                    var command = rec.getCommand();
                    var processor = YamcsPlugin.getProcessorClient();
                    var value = Value.newBuilder().setType(Type.STRING).setStringValue(newComment).build();
                    processor.updateCommand(command.getName(), command.getId(), "Comment", value).exceptionally(t -> {
                        Display.getDefault().asyncExec(() -> {
                            var dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                            dialog.setText("Comment Update");
                            dialog.setMessage("Comment has not been updated. Details: " + t.getMessage());
                            // open dialog and await user selection
                            dialog.open();
                        });
                        return null;
                    });
                }
            }
        }
        return null;
    }
}
