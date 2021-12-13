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
import static org.csstudio.opibuilder.widgets.model.AbstractShapeModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.PolyLineModel.PROP_ARROW;
import static org.csstudio.opibuilder.widgets.model.PolyLineModel.PROP_ARROW_LENGTH;
import static org.csstudio.opibuilder.widgets.model.PolyLineModel.PROP_FILL_ARROW;

import org.csstudio.opibuilder.widgets.model.PolyLineModel;
import org.csstudio.swt.widgets.figures.PolylineFigure;
import org.csstudio.swt.widgets.figures.PolylineFigure.ArrowType;
import org.eclipse.draw2d.IFigure;

/**
 * Editpart for polyline widget.
 */
public final class PolylineEditPart extends AbstractPolyEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var polyline = new PolylineFigure();
        var model = getWidgetModel();
        polyline.setPoints(model.getPoints());
        polyline.setFill(model.getFillLevel());
        polyline.setHorizontalFill(model.isHorizontalFill());
        polyline.setTransparent(model.isTransparent());
        polyline.setArrowLineLength(model.getArrowLength());
        polyline.setArrowType(ArrowType.values()[model.getArrowType()]);
        polyline.setFillArrow(model.isFillArrow());

        return polyline;
    }

    @Override
    public PolyLineModel getWidgetModel() {
        return (PolyLineModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_FILL_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var polyline = (PolylineFigure) refreshableFigure;
            polyline.setFill((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL_FILL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (PolylineFigure) refreshableFigure;
            figure.setHorizontalFill((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (PolylineFigure) refreshableFigure;
            figure.setTransparent((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_ARROW, (oldValue, newValue, refreshableFigure) -> {
            var figure = (PolylineFigure) refreshableFigure;
            figure.setArrowType(ArrowType.values()[(Integer) newValue]);
            getWidgetModel().updateBounds();
            return true;
        });

        setPropertyChangeHandler(PROP_ARROW_LENGTH, (oldValue, newValue, refreshableFigure) -> {
            var figure = (PolylineFigure) refreshableFigure;
            figure.setArrowLineLength((Integer) newValue);
            getWidgetModel().updateBounds();
            return true;
        });

        setPropertyChangeHandler(PROP_FILL_ARROW, (oldValue, newValue, refreshableFigure) -> {
            var figure = (PolylineFigure) refreshableFigure;
            figure.setFillArrow((Boolean) newValue);
            return true;
        });
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((PolylineFigure) getFigure()).setFill(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        return ((PolylineFigure) getFigure()).getFill();
    }
}
