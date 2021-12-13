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

import static org.csstudio.opibuilder.widgets.model.ThermometerModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.ThermometerModel.PROP_FILLBACKGROUND_COLOR;
import static org.csstudio.opibuilder.widgets.model.ThermometerModel.PROP_FILLCOLOR_ALARM_SENSITIVE;
import static org.csstudio.opibuilder.widgets.model.ThermometerModel.PROP_FILL_COLOR;
import static org.csstudio.opibuilder.widgets.model.ThermometerModel.PROP_SHOW_BULB;
import static org.csstudio.opibuilder.widgets.model.ThermometerModel.PROP_UNIT;

import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.ThermometerModel;
import org.csstudio.swt.widgets.figures.ThermometerFigure;
import org.csstudio.swt.widgets.figures.ThermometerFigure.TemperatureUnit;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the Thermometer widget. The controller mediates between {@link ThermometerModel} and
 * {@link ThermometerFigure}.
 */
public final class ThermometerEditPart extends AbstractMarkedWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var thermometer = new ThermometerFigure();

        initializeCommonFigureProperties(thermometer, model);
        thermometer.setFillColor(model.getFillColor());
        thermometer.setTemperatureUnit(model.getUnit());
        thermometer.setShowBulb(model.isShowBulb());
        thermometer.setFillBackgroundColor(model.getFillbackgroundColor());
        thermometer.setEffect3D(model.isEffect3D());
        return thermometer;
    }

    @Override
    public ThermometerModel getWidgetModel() {
        return (ThermometerModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_FILL_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setFillColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_FILLBACKGROUND_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setFillBackgroundColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_BULB, (oldValue, newValue, refreshableFigure) -> {
            var thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setShowBulb((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_UNIT, (oldValue, newValue, refreshableFigure) -> {
            var thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setTemperatureUnit(TemperatureUnit.values()[(Integer) newValue]);
            return false;
        });

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var thermo = (ThermometerFigure) refreshableFigure;
            thermo.setEffect3D((Boolean) newValue);
            return false;
        });

        // Change fill color when "FillColor Alarm Sensitive" property changes.
        setPropertyChangeHandler(PROP_FILLCOLOR_ALARM_SENSITIVE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ThermometerFigure) refreshableFigure;
            boolean sensitive = (Boolean) newValue;
            figure.setFillColor(delegate.calculateAlarmColor(sensitive, getWidgetModel().getFillColor()));
            return true;
        });

        // Change fill color when alarm severity changes.
        delegate.addAlarmSeverityListener((severity, figure) -> {
            if (!getWidgetModel().isFillColorAlarmSensitive()) {
                return false;
            }
            var thermo = (ThermometerFigure) figure;
            thermo.setFillColor(delegate.calculateAlarmColor(getWidgetModel().isFillColorAlarmSensitive(),
                    getWidgetModel().getFillColor()));
            return true;
        });
    }
}
