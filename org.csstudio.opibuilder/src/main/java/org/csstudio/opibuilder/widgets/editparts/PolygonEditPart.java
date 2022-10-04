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

import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.PolygonModel;
import org.csstudio.swt.widgets.figures.PolygonFigure;
import org.eclipse.draw2d.IFigure;

/**
 * Editpart of polygon widget.
 */
public final class PolygonEditPart extends AbstractPolyEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var polygon = new PolygonFigure();
        var model = getWidgetModel();
        polygon.setPoints(model.getPoints());
        polygon.setFill(model.getFillLevel());
        polygon.setHorizontalFill(model.isHorizontalFill());
        polygon.setTransparent(model.isTransparent());
        polygon.setLineColor(model.getLineColor());
        return polygon;
    }

    @Override
    public PolygonModel getWidgetModel() {
        return (PolygonModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_FILL_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var polygon = (PolygonFigure) refreshableFigure;
            polygon.setFill((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL_FILL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (PolygonFigure) refreshableFigure;
            figure.setHorizontalFill((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (PolygonFigure) refreshableFigure;
            figure.setTransparent((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LINE_COLOR, (oldValue, newValue, refreshableFigure) -> {
            ((PolygonFigure) refreshableFigure).setLineColor(((OPIColor) newValue).getSWTColor());
            return true;
        });
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((PolygonFigure) getFigure()).setFill(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        return ((PolygonFigure) getFigure()).getFill();
    }
}
