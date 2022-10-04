/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
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
import static org.csstudio.opibuilder.widgets.model.EllipseModel.PROP_BACKGROUND_GRADIENT_START_COLOR;
import static org.csstudio.opibuilder.widgets.model.EllipseModel.PROP_FOREGROUND_GRADIENT_START_COLOR;
import static org.csstudio.opibuilder.widgets.model.EllipseModel.PROP_GRADIENT;

import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.EllipseModel;
import org.csstudio.swt.widgets.figures.EllipseFigure;
import org.eclipse.draw2d.IFigure;

/**
 * The controller for ellipse widget.
 */
public class EllipseEditpart extends AbstractShapeEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var figure = new EllipseFigure();
        var model = getWidgetModel();
        figure.setFill(model.getFillLevel());
        figure.setHorizontalFill(model.isHorizontalFill());
        figure.setTransparent(model.isTransparent());
        figure.setLineColor(model.getLineColor());
        figure.setGradient(model.isGradient());
        figure.setBackGradientStartColor(model.getBackgroundGradientStartColor());
        figure.setForeGradientStartColor(model.getForegroundGradientStartColor());
        return figure;
    }

    @Override
    public EllipseModel getWidgetModel() {
        return (EllipseModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_FILL_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var ellipseFigure = (EllipseFigure) refreshableFigure;
            ellipseFigure.setFill((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL_FILL, (oldValue, newValue, refreshableFigure) -> {
            var ellipseFigure = (EllipseFigure) refreshableFigure;
            ellipseFigure.setHorizontalFill((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, refreshableFigure) -> {
            var ellipseFigure = (EllipseFigure) refreshableFigure;
            ellipseFigure.setTransparent((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LINE_COLOR, (oldValue, newValue, refreshableFigure) -> {
            ((EllipseFigure) refreshableFigure).setLineColor(((OPIColor) newValue).getSWTColor());
            return true;
        });

        setPropertyChangeHandler(PROP_GRADIENT, (oldValue, newValue, figure) -> {
            ((EllipseFigure) figure).setGradient((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_BACKGROUND_GRADIENT_START_COLOR, (oldValue, newValue, figure) -> {
            ((EllipseFigure) figure).setBackGradientStartColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_FOREGROUND_GRADIENT_START_COLOR, (oldValue, newValue, figure) -> {
            ((EllipseFigure) figure).setForeGradientStartColor(((OPIColor) newValue).getSWTColor());
            return false;
        });
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((EllipseFigure) getFigure()).setFill(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        return ((EllipseFigure) getFigure()).getFill();
    }
}
