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

import static org.csstudio.opibuilder.widgets.model.AbstractShapeModel.PROP_FILL_LEVEL;
import static org.csstudio.opibuilder.widgets.model.AbstractShapeModel.PROP_HORIZONTAL_FILL;
import static org.csstudio.opibuilder.widgets.model.AbstractShapeModel.PROP_LINE_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractShapeModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.RoundedRectangleModel.PROP_BACKGROUND_GRADIENT_START_COLOR;
import static org.csstudio.opibuilder.widgets.model.RoundedRectangleModel.PROP_CORNER_HEIGHT;
import static org.csstudio.opibuilder.widgets.model.RoundedRectangleModel.PROP_CORNER_WIDTH;
import static org.csstudio.opibuilder.widgets.model.RoundedRectangleModel.PROP_FOREGROUND_GRADIENT_START_COLOR;
import static org.csstudio.opibuilder.widgets.model.RoundedRectangleModel.PROP_GRADIENT;

import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.RoundedRectangleModel;
import org.csstudio.swt.widgets.figures.RoundedRectangleFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * The editpart of a rectangle widget.
 */
public class RoundedRectangleEditpart extends AbstractShapeEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var figure = new RoundedRectangleFigure();
        var model = getWidgetModel();
        figure.setFill(model.getFillLevel());
        figure.setHorizontalFill(model.isHorizontalFill());
        figure.setTransparent(model.isTransparent());
        figure.setCornerDimensions(new Dimension(model.getCornerWidth(), model.getCornerHeight()));
        figure.setLineColor(model.getLineColor());
        figure.setGradient(model.isGradient());
        figure.setBackGradientStartColor(model.getBackgroundGradientStartColor());
        figure.setForeGradientStartColor(model.getForegroundGradientStartColor());

        return figure;
    }

    @Override
    public RoundedRectangleModel getWidgetModel() {
        return (RoundedRectangleModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_FILL_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (RoundedRectangleFigure) refreshableFigure;
            figure.setFill((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL_FILL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (RoundedRectangleFigure) refreshableFigure;
            figure.setHorizontalFill((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (RoundedRectangleFigure) refreshableFigure;
            figure.setTransparent((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LINE_COLOR, (oldValue, newValue, refreshableFigure) -> {
            ((RoundedRectangleFigure) refreshableFigure).setLineColor(((OPIColor) newValue).getSWTColor());
            return true;
        });

        setPropertyChangeHandler(PROP_CORNER_WIDTH, (oldValue, newValue, refreshableFigure) -> {
            var figure = (RoundedRectangleFigure) refreshableFigure;
            figure.setCornerWidth((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_CORNER_HEIGHT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (RoundedRectangleFigure) refreshableFigure;
            figure.setCornerHeight((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_GRADIENT, (oldValue, newValue, figure) -> {
            ((RoundedRectangleFigure) figure).setGradient((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_BACKGROUND_GRADIENT_START_COLOR, (oldValue, newValue, figure) -> {
            ((RoundedRectangleFigure) figure).setBackGradientStartColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_FOREGROUND_GRADIENT_START_COLOR, (oldValue, newValue, figure) -> {
            ((RoundedRectangleFigure) figure).setForeGradientStartColor(((OPIColor) newValue).getSWTColor());
            return false;
        });
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((RoundedRectangleFigure) getFigure()).setFill(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        return ((RoundedRectangleFigure) getFigure()).getFill();
    }
}
