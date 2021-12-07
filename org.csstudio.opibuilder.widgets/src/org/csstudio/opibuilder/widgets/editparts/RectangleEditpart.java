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

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractShapeModel;
import org.csstudio.opibuilder.widgets.model.RectangleModel;
import org.csstudio.opibuilder.widgets.model.RoundedRectangleModel;
import org.csstudio.swt.widgets.figures.OPIRectangleFigure;
import org.eclipse.draw2d.IFigure;

/**
 * The editpart of a rectangle widget.
 */
public class RectangleEditpart extends AbstractShapeEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var figure = new OPIRectangleFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
        var model = getWidgetModel();
        figure.setFill(model.getFillLevel());
        figure.setHorizontalFill(model.isHorizontalFill());
        figure.setTransparent(model.isTransparent());
        figure.setSelectable(determineSelectable());
        figure.setLineColor(model.getLineColor());
        figure.setGradient(model.isGradient());
        figure.setBackGradientStartColor(model.getBackgroundGradientStartColor());
        figure.setForeGradientStartColor(model.getForegroundGradientStartColor());

        return figure;
    }

    @Override
    public RectangleModel getWidgetModel() {
        return (RectangleModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();
        // fill
        IWidgetPropertyChangeHandler fillHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                var figure = (OPIRectangleFigure) refreshableFigure;
                figure.setFill((Double) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_FILL_LEVEL, fillHandler);

        // fill orientaion
        IWidgetPropertyChangeHandler fillOrientHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                var figure = (OPIRectangleFigure) refreshableFigure;
                figure.setHorizontalFill((Boolean) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_HORIZONTAL_FILL, fillOrientHandler);

        // transparent
        IWidgetPropertyChangeHandler transparentHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                var figure = (OPIRectangleFigure) refreshableFigure;
                figure.setTransparent((Boolean) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(RectangleModel.PROP_TRANSPARENT, transparentHandler);

        // line color
        IWidgetPropertyChangeHandler lineColorHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                ((OPIRectangleFigure) refreshableFigure).setLineColor(((OPIColor) newValue).getSWTColor());
                return true;
            }
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_LINE_COLOR, lineColorHandler);

        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                ((OPIRectangleFigure) figure).setGradient((Boolean) newValue);
                return false;
            }
        };
        setPropertyChangeHandler(RectangleModel.PROP_GRADIENT, handler);

        handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                ((OPIRectangleFigure) figure).setBackGradientStartColor(((OPIColor) newValue).getSWTColor());
                return false;
            }
        };
        setPropertyChangeHandler(RectangleModel.PROP_BACKGROUND_GRADIENT_START_COLOR, handler);

        handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                ((OPIRectangleFigure) figure).setForeGradientStartColor(((OPIColor) newValue).getSWTColor());
                return false;
            }
        };
        setPropertyChangeHandler(RoundedRectangleModel.PROP_FOREGROUND_GRADIENT_START_COLOR, handler);

    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((OPIRectangleFigure) getFigure()).setFill(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        return ((OPIRectangleFigure) getFigure()).getFill();
    }

    /**
     * The rectangle is selectable if it has any actions, has a tooltip defined or is opaque. A transparent rectangle
     * should pass clicks or tooltips through.
     * 
     * @return whether the rectangle is selectable
     */
    private boolean determineSelectable() {
        var hasActions = !getWidgetModel().getActionsInput().getActionsList().isEmpty();
        var hasTooltip = getWidgetModel().getTooltip().trim().length() > 0;
        var opaque = !getWidgetModel().isTransparent();
        return hasActions || hasTooltip || opaque;
    }

}
