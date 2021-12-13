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
import static org.csstudio.opibuilder.widgets.model.GaugeModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.GaugeModel.PROP_NEEDLE_COLOR;
import static org.csstudio.opibuilder.widgets.model.GaugeModel.PROP_RAMP_GRADIENT;

import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.GaugeModel;
import org.csstudio.swt.widgets.figures.GaugeFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the Gauge widget. The controller mediates between {@link GaugeModel} and {@link GaugeFigure}.
 */
public final class GaugeEditPart extends AbstractMarkedWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var gauge = new GaugeFigure();

        initializeCommonFigureProperties(gauge, model);
        gauge.setNeedleColor(CustomMediaFactory.getInstance().getColor((model.getNeedleColor())));
        gauge.setEffect3D(model.isEffect3D());
        gauge.setGradient(model.isRampGradient());

        return gauge;
    }

    @Override
    public GaugeModel getWidgetModel() {
        return (GaugeModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_NEEDLE_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var gauge = (GaugeFigure) refreshableFigure;
            gauge.setNeedleColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var gauge = (GaugeFigure) refreshableFigure;
            gauge.setEffect3D((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_RAMP_GRADIENT, (oldValue, newValue, refreshableFigure) -> {
            var gauge = (GaugeFigure) refreshableFigure;
            gauge.setGradient((Boolean) newValue);
            return false;
        });

        IWidgetPropertyChangeHandler sizeHandler = (oldValue, newValue, figure) -> {
            if (((Integer) newValue) < GaugeModel.MINIMUM_SIZE) {
                newValue = GaugeModel.MINIMUM_SIZE;
            }
            getWidgetModel().setSize((Integer) newValue, (Integer) newValue);
            return false;
        };
        PropertyChangeListener sizeListener = evt -> sizeHandler.handleChange(evt.getOldValue(), evt.getNewValue(),
                getFigure());
        getWidgetModel().getProperty(PROP_WIDTH).addPropertyChangeListener(sizeListener);
        getWidgetModel().getProperty(PROP_HEIGHT).addPropertyChangeListener(sizeListener);
    }
}
