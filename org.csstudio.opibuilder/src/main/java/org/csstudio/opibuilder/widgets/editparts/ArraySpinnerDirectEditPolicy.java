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

import java.text.DecimalFormat;
import java.text.ParseException;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

/**
 * The Editpolicy to handle direct text edit in index spinner of an array widget.
 */
public class ArraySpinnerDirectEditPolicy extends DirectEditPolicy {

    @Override
    protected Command getDirectEditCommand(DirectEditRequest edit) {
        var text = (String) edit.getCellEditor().getValue();
        text = text.replace("e", "E");
        try {
            var value = new DecimalFormat().parse(text).intValue();
            var array = (ArrayEditPart) getHost();
            var command = new ArraySpinnerEditCommand(array, value);
            return command;
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    protected void showCurrentEditValue(DirectEditRequest request) {
        // String value = (String)request.getCellEditor().getValue();
        // ((LabelFigure)getHostFigure()).setText(value);
        // hack to prevent async layout from placing the cell editor twice.
        // getHostFigure().getUpdateManager().performUpdate();
    }

    static class ArraySpinnerEditCommand extends Command {

        private int newIndex, oldIndex;
        private ArrayEditPart arrayEditpart;

        public ArraySpinnerEditCommand(ArrayEditPart arrayEditpart, int newIndex) {
            this.arrayEditpart = arrayEditpart;
            this.newIndex = newIndex;
        }

        @Override
        public void execute() {
            oldIndex = arrayEditpart.getArrayFigure().getIndex();
            if (newIndex >= arrayEditpart.getArrayFigure().getArrayLength()) {
                newIndex = arrayEditpart.getArrayFigure().getArrayLength() - 1;
            }
            arrayEditpart.getArrayFigure().setIndex(newIndex);
        }

        @Override
        public void undo() {
            arrayEditpart.getArrayFigure().setIndex(oldIndex);
        }
    }
}
