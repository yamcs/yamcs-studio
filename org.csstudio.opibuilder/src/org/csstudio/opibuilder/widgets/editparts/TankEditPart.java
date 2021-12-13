/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.widgets.model.TankModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.TankModel.PROP_FILLBACKGROUND_COLOR;
import static org.csstudio.opibuilder.widgets.model.TankModel.PROP_FILLCOLOR_ALARM_SENSITIVE;
import static org.csstudio.opibuilder.widgets.model.TankModel.PROP_FILL_COLOR;

import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.TankModel;
import org.csstudio.swt.widgets.figures.TankFigure;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the tank widget. The controller mediates between {@link TankModel} and {@link TankFigure}.
 */
public final class TankEditPart extends AbstractMarkedWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var tank = new TankFigure();

        initializeCommonFigureProperties(tank, model);
        tank.setFillColor(model.getFillColor());
        tank.setEffect3D(model.isEffect3D());
        tank.setFillBackgroundColor(model.getFillbackgroundColor());
        return tank;
    }

    @Override
    public TankModel getWidgetModel() {
        return (TankModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_FILL_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var tank = (TankFigure) refreshableFigure;
            tank.setFillColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_FILLBACKGROUND_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var tank = (TankFigure) refreshableFigure;
            tank.setFillBackgroundColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var tank = (TankFigure) refreshableFigure;
            tank.setEffect3D((Boolean) newValue);
            return false;
        });

        // Change fill color when "FillColor Alarm Sensitive" property changes.
        setPropertyChangeHandler(PROP_FILLCOLOR_ALARM_SENSITIVE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (TankFigure) refreshableFigure;
            var sensitive = (Boolean) newValue;
            figure.setFillColor(delegate.calculateAlarmColor(sensitive, getWidgetModel().getFillColor()));
            return true;
        });

        // Change fill color when alarm severity changes.
        delegate.addAlarmSeverityListener((severity, figure) -> {
            if (!getWidgetModel().isFillColorAlarmSensitive()) {
                return false;
            }
            var tank = (TankFigure) figure;
            tank.setFillColor(delegate.calculateAlarmColor(getWidgetModel().isFillColorAlarmSensitive(),
                    getWidgetModel().getFillColor()));
            return true;
        });
    }
}
