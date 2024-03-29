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

import static org.csstudio.opibuilder.widgets.model.ArcModel.PROP_FILL;
import static org.csstudio.opibuilder.widgets.model.ArcModel.PROP_START_ANGLE;
import static org.csstudio.opibuilder.widgets.model.ArcModel.PROP_TOTAL_ANGLE;

import org.csstudio.opibuilder.widgets.model.ArcModel;
import org.csstudio.swt.widgets.figures.ArcFigure;
import org.eclipse.draw2d.IFigure;

/**
 * The controller for arc widget.
 */
public class ArcEditpart extends AbstractShapeEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var figure = new ArcFigure();
        var model = getWidgetModel();
        figure.setFill(model.isFill());
        figure.setStartAngle(model.getStartAngle());
        figure.setTotalAngle(model.getTotalAngle());
        return figure;
    }

    @Override
    public ArcModel getWidgetModel() {
        return (ArcModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();
        setPropertyChangeHandler(PROP_FILL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ArcFigure) refreshableFigure;
            figure.setFill((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_START_ANGLE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ArcFigure) refreshableFigure;
            figure.setStartAngle((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_TOTAL_ANGLE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ArcFigure) refreshableFigure;
            figure.setTotalAngle((Integer) newValue);
            return true;
        });
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Boolean) {
            ((ArcFigure) getFigure()).setFill((Boolean) value);
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        return ((ArcFigure) getFigure()).isFill();
    }
}
