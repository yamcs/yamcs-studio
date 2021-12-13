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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.widgets.model.ActionButtonModel.PROP_IMAGE;
import static org.csstudio.opibuilder.widgets.model.ActionButtonModel.PROP_TEXT;
import static org.csstudio.opibuilder.widgets.model.ActionButtonModel.PROP_TOGGLE_BUTTON;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.OpenDisplayAction;
import org.csstudio.opibuilder.widgets.model.ActionButtonModel;
import org.csstudio.swt.widgets.figures.ActionButtonFigure;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;

/**
 * EditPart controller for the ActioButton widget. The controller mediates between {@link ActionButtonModel} and
 * {@link ActionButtonFigure2}.
 */
public class Draw2DButtonEditPartDelegate implements IButtonEditPartDelegate {

    private ActionButtonEditPart editpart;

    public Draw2DButtonEditPartDelegate(ActionButtonEditPart editpart) {
        this.editpart = editpart;
    }

    @Override
    public IFigure doCreateFigure() {
        var model = editpart.getWidgetModel();

        var buttonFigure = new ActionButtonFigure(editpart.getExecutionMode() == ExecutionMode.RUN_MODE);
        buttonFigure.setText(model.getText());
        buttonFigure.setToggleStyle(model.isToggleButton());
        buttonFigure.setImagePath(model.getImagePath());
        editpart.updatePropSheet();
        return buttonFigure;
    }

    @Override
    public void hookMouseClickAction() {
        ((ActionButtonFigure) editpart.getFigure()).addActionListener(mouseEventState -> {
            var actions = editpart.getHookedActions();
            if (actions != null) {
                for (var action : actions) {
                    if (action instanceof OpenDisplayAction) {
                        ((OpenDisplayAction) action).runWithModifiers((mouseEventState & SWT.CONTROL) != 0,
                                (mouseEventState & SWT.SHIFT) != 0);
                    } else {
                        action.run();
                    }
                }
            }
        });
    }

    @Override
    public void deactivate() {
        ((ActionButtonFigure) editpart.getFigure()).dispose();
    }

    @Override
    public void registerPropertyChangeHandlers() {
        editpart.setPropertyChangeHandler(PROP_TEXT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ActionButtonFigure) refreshableFigure;
            figure.setText(newValue.toString());
            figure.calculateTextPosition();
            return true;
        });

        editpart.setPropertyChangeHandler(PROP_IMAGE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ActionButtonFigure) refreshableFigure;
            var absolutePath = (String) newValue;
            if (absolutePath != null && !absolutePath.contains("://")) {
                var path = Path.fromPortableString(absolutePath);
                if (!path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(editpart.getWidgetModel(), path);
                    absolutePath = path.toPortableString();
                }
            }
            figure.setImagePath(absolutePath);
            return true;
        });

        editpart.setPropertyChangeHandler(PROP_WIDTH, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ActionButtonFigure) refreshableFigure;
            var height = (Integer) editpart.getPropertyValue(PROP_HEIGHT);
            figure.calculateTextPosition((Integer) newValue, height);
            return true;
        });

        editpart.setPropertyChangeHandler(PROP_HEIGHT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ActionButtonFigure) refreshableFigure;
            var width = (Integer) editpart.getPropertyValue(PROP_WIDTH);
            figure.calculateTextPosition(width, (Integer) newValue);
            return true;
        });

        IWidgetPropertyChangeHandler buttonStyleHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (ActionButtonFigure) refreshableFigure;
            figure.setToggleStyle((Boolean) newValue);
            editpart.updatePropSheet();
            return true;
        };
        editpart.getWidgetModel().getProperty(PROP_TOGGLE_BUTTON).addPropertyChangeListener(
                evt -> buttonStyleHandler.handleChange(evt.getOldValue(), evt.getNewValue(), editpart.getFigure()));
    }

    @Override
    public void setValue(Object value) {
        ((ActionButtonFigure) editpart.getFigure()).setText(value.toString());
    }

    @Override
    public Object getValue() {
        return ((ActionButtonFigure) editpart.getFigure()).getText();
    }

    @Override
    public boolean isSelected() {
        return ((ActionButtonFigure) editpart.getFigure()).isSelected();
    }
}
