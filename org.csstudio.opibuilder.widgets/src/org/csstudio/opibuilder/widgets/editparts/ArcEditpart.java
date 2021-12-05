/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.ArcModel;
import org.csstudio.swt.widgets.figures.ArcFigure;
import org.eclipse.draw2d.IFigure;

/**
 * The controller for arc widget.
 */
public class ArcEditpart extends AbstractShapeEditPart {

    @Override
    protected IFigure doCreateFigure() {
        ArcFigure figure = new ArcFigure();
        ArcModel model = getWidgetModel();
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
        // fill
        IWidgetPropertyChangeHandler fillHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                ArcFigure figure = (ArcFigure) refreshableFigure;
                figure.setFill((Boolean) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(ArcModel.PROP_FILL, fillHandler);

        // start angle
        IWidgetPropertyChangeHandler startAngleHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                ArcFigure figure = (ArcFigure) refreshableFigure;
                figure.setStartAngle((Integer) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(ArcModel.PROP_START_ANGLE, startAngleHandler);

        // total angle
        IWidgetPropertyChangeHandler totalAngleHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                ArcFigure figure = (ArcFigure) refreshableFigure;
                figure.setTotalAngle((Integer) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(ArcModel.PROP_TOTAL_ANGLE, totalAngleHandler);

    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Boolean) {
            ((ArcFigure) getFigure()).setFill((Boolean) value);
        } else
            super.setValue(value);
    }

    @Override
    public Object getValue() {
        return ((ArcFigure) getFigure()).isFill();
    }

}
