/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractShapeModel;
import org.csstudio.opibuilder.widgets.model.EllipseModel;
import org.csstudio.swt.widgets.figures.EllipseFigure;
import org.eclipse.draw2d.IFigure;

/**
 * The controller for ellipse widget.
 */
public class EllipseEditpart extends AbstractShapeEditPart {

    @Override
    protected IFigure doCreateFigure() {
        EllipseFigure figure = new EllipseFigure();
        EllipseModel model = getWidgetModel();
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
        // fill
        IWidgetPropertyChangeHandler fillHandler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                EllipseFigure ellipseFigure = (EllipseFigure) refreshableFigure;
                ellipseFigure.setFill((Double) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_FILL_LEVEL, fillHandler);

        // fill orientaion
        IWidgetPropertyChangeHandler fillOrientHandler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                EllipseFigure ellipseFigure = (EllipseFigure) refreshableFigure;
                ellipseFigure.setHorizontalFill((Boolean) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_HORIZONTAL_FILL, fillOrientHandler);

        // transparent
        IWidgetPropertyChangeHandler transparentHandler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                EllipseFigure ellipseFigure = (EllipseFigure) refreshableFigure;
                ellipseFigure.setTransparent((Boolean) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_TRANSPARENT, transparentHandler);

        // line color
        IWidgetPropertyChangeHandler lineColorHandler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                ((EllipseFigure) refreshableFigure).setLineColor(
                        ((OPIColor) newValue).getSWTColor());
                return true;
            }
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_LINE_COLOR,
                lineColorHandler);

        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                ((EllipseFigure) figure).setGradient((Boolean) newValue);
                return false;
            }
        };
        setPropertyChangeHandler(EllipseModel.PROP_GRADIENT, handler);

        handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                ((EllipseFigure) figure).setBackGradientStartColor(((OPIColor) newValue).getSWTColor());
                return false;
            }
        };
        setPropertyChangeHandler(EllipseModel.PROP_BACKGROUND_GRADIENT_START_COLOR, handler);

        handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                ((EllipseFigure) figure).setForeGradientStartColor(((OPIColor) newValue).getSWTColor());
                return false;
            }
        };
        setPropertyChangeHandler(EllipseModel.PROP_FOREGROUND_GRADIENT_START_COLOR, handler);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((EllipseFigure) getFigure()).setFill(((Number) value).doubleValue());
        } else
            super.setValue(value);
    }

    @Override
    public Object getValue() {
        return ((EllipseFigure) getFigure()).getFill();
    }

}
