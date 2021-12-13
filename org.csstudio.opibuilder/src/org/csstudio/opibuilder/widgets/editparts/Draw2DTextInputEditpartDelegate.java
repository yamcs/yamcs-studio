/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TEXT;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_DATETIME_FORMAT;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_FILE_RETURN_PART;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_FILE_SOURCE;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_SELECTOR_TYPE;

import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.csstudio.swt.widgets.figures.TextInputFigure;
import org.csstudio.swt.widgets.figures.TextInputFigure.FileReturnPart;
import org.csstudio.swt.widgets.figures.TextInputFigure.FileSource;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

/**
 * The editpart delegate for draw2d text input widget.
 */
public class Draw2DTextInputEditpartDelegate implements ITextInputEditPartDelegate {

    private TextInputEditpart editpart;
    private TextInputModel model;
    private TextInputFigure textInputFigure;

    /**
     * @param superFigure
     *            the figure created by super.doCreateFigure().
     */
    public Draw2DTextInputEditpartDelegate(TextInputEditpart editpart, TextInputModel model,
            TextInputFigure superFigure) {
        this.editpart = editpart;
        this.model = model;
        this.textInputFigure = superFigure;
    }

    @Override
    public IFigure doCreateFigure() {
        textInputFigure.setSelectorType(model.getSelectorType());
        textInputFigure.setDateTimeFormat(model.getDateTimeFormat());
        textInputFigure.setFileSource(model.getFileSource());
        textInputFigure.setFileReturnPart(model.getFileReturnPart());

        textInputFigure.addManualValueChangeListener(newValue -> outputText(newValue));

        return textInputFigure;
    }

    /**
     * Call this method when user hit Enter or Ctrl+Enter for multiline input.
     */
    protected void outputText(String newValue) {
        if (editpart.getExecutionMode() == ExecutionMode.RUN_MODE) {
            editpart.setPVValue(PROP_PVNAME, newValue);
            model.setPropertyValue(PROP_TEXT, newValue, false);
        } else {
            editpart.getViewer().getEditDomain().getCommandStack()
                    .execute(new SetWidgetPropertyCommand(model, PROP_TEXT, newValue));
        }
    }

    @Override
    public void createEditPolicies() {
        editpart.installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TextUpdateDirectEditPolicy());
    }

    @Override
    public void registerPropertyChangeHandlers() {
        editpart.setPropertyChangeHandler(PROP_SELECTOR_TYPE, (oldValue, newValue, figure) -> {
            ((TextInputFigure) figure).setSelectorType(model.getSelectorType());
            return false;
        });

        editpart.setPropertyChangeHandler(PROP_DATETIME_FORMAT, (oldValue, newValue, figure) -> {
            ((TextInputFigure) figure).setDateTimeFormat((String) newValue);
            return false;
        });

        editpart.setPropertyChangeHandler(PROP_FILE_SOURCE, (oldValue, newValue, figure) -> {
            ((TextInputFigure) figure).setFileSource(FileSource.values()[(Integer) newValue]);
            return false;
        });

        editpart.setPropertyChangeHandler(PROP_FILE_RETURN_PART, (oldValue, newValue, figure) -> {
            ((TextInputFigure) figure).setFileReturnPart(FileReturnPart.values()[(Integer) newValue]);
            return false;
        });
    }

    @Override
    public void updatePropSheet() {
        switch (model.getSelectorType()) {
        case NONE:
            model.setPropertyVisible(PROP_DATETIME_FORMAT, false);
            model.setPropertyVisible(PROP_FILE_RETURN_PART, false);
            model.setPropertyVisible(PROP_FILE_SOURCE, false);
            break;
        case DATETIME:
            model.setPropertyVisible(PROP_DATETIME_FORMAT, true);
            model.setPropertyVisible(PROP_FILE_RETURN_PART, false);
            model.setPropertyVisible(PROP_FILE_SOURCE, false);
            break;
        case FILE:
            model.setPropertyVisible(PROP_DATETIME_FORMAT, false);
            model.setPropertyVisible(PROP_FILE_RETURN_PART, true);
            model.setPropertyVisible(PROP_FILE_SOURCE, true);
            break;
        default:
            break;
        }
    }
}
