/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.widgets.model.LabelModel;
import org.csstudio.opibuilder.widgets.model.TextUpdateModel;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

/**
 * The Editpolicy to handle direct text edit in spinner.
 */
public class SpinnerDirectEditPolicy extends DirectEditPolicy {

    @Override
    protected Command getDirectEditCommand(DirectEditRequest edit) {
        var labelText = (String) edit.getCellEditor().getValue();
        var spinner = (SpinnerEditpart) getHost();
        var command = new SpinnerEditCommand((LabelModel) spinner.getModel(), labelText);
        return command;
    }

    @Override
    protected void showCurrentEditValue(DirectEditRequest request) {
        // String value = (String)request.getCellEditor().getValue();
        // ((LabelFigure)getHostFigure()).setText(value);
        // hack to prevent async layout from placing the cell editor twice.
        // getHostFigure().getUpdateManager().performUpdate();
    }

    static class SpinnerEditCommand extends Command {

        private String newText, oldText;
        private LabelModel label;

        public SpinnerEditCommand(LabelModel l, String s) {
            label = l;
            if (s != null) {
                newText = s;
            } else {
                newText = "";
            }
        }

        @Override
        public void execute() {
            oldText = label.getText();
            label.setPropertyValue(TextUpdateModel.PROP_TEXT, newText, true);
        }

        @Override
        public void undo() {
            label.setText(oldText);
        }
    }
}
