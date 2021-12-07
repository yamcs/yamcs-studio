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
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.MeterModel;
import org.csstudio.swt.widgets.figures.MeterFigure;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the Gauge widget. The controller mediates between {@link MeterModel} and {@link MeterFigure}.
 */
public final class MeterEditPart extends AbstractMarkedWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var xMeter = new MeterFigure();

        initializeCommonFigureProperties(xMeter, model);
        xMeter.setNeedleColor((model.getNeedleColor()));
        xMeter.setGradient(model.isRampGradient());
        xMeter.setValueLabelVisibility(model.isShowValueLabelVisible());

        return xMeter;
    }

    @Override
    public MeterModel getWidgetModel() {
        return (MeterModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        // needle Color
        IWidgetPropertyChangeHandler needleColorColorHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                var xMeter = (MeterFigure) refreshableFigure;
                xMeter.setNeedleColor(((OPIColor) newValue).getSWTColor());
                return false;
            }
        };
        setPropertyChangeHandler(MeterModel.PROP_NEEDLE_COLOR, needleColorColorHandler);

        // Ramp gradient
        IWidgetPropertyChangeHandler gradientHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                var xMeter = (MeterFigure) refreshableFigure;
                xMeter.setGradient((Boolean) newValue);
                return false;
            }
        };
        setPropertyChangeHandler(MeterModel.PROP_RAMP_GRADIENT, gradientHandler);

        // Show Value Label
        IWidgetPropertyChangeHandler valueLabelHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                var xMeter = (MeterFigure) refreshableFigure;
                xMeter.setValueLabelVisibility((Boolean) newValue);
                return false;
            }
        };
        setPropertyChangeHandler(MeterModel.PROP_SHOW_VALUE_LABEL, valueLabelHandler);

    }

}
