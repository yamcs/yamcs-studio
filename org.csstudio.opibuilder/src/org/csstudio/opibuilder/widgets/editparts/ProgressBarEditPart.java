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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ENABLED;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_FILLBACKGROUND_COLOR;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_FILLCOLOR_ALARM_SENSITIVE;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_FILL_COLOR;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_HORIZONTAL;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_INDICATOR_MODE;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_ORIGIN;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_ORIGIN_IGNORED;
import static org.csstudio.opibuilder.widgets.model.ProgressBarModel.PROP_SHOW_LABEL;

import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.ProgressBarModel;
import org.csstudio.opibuilder.widgets.model.ScaledSliderModel;
import org.csstudio.swt.widgets.figures.ProgressBarFigure;
import org.csstudio.swt.widgets.figures.ScaledSliderFigure;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the scaled slider widget. The controller mediates between {@link ScaledSliderModel} and
 * {@link ScaledSliderFigure}.
 */
public final class ProgressBarEditPart extends AbstractMarkedWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var bar = new ProgressBarFigure();

        initializeCommonFigureProperties(bar, model);
        bar.setFillColor(model.getFillColor());
        bar.setEffect3D(model.isEffect3D());
        bar.setFillBackgroundColor(model.getFillbackgroundColor());
        bar.setHorizontal(model.isHorizontal());
        bar.setShowLabel(model.isShowLabel());
        bar.setOrigin(model.getOrigin());
        bar.setOriginIgnored(model.isOriginIgnored());
        bar.setIndicatorMode(model.isIndicatorMode());
        return bar;
    }

    @Override
    public ProgressBarModel getWidgetModel() {
        return (ProgressBarModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_ORIGIN, (oldValue, newValue, figure) -> {
            ((ProgressBarFigure) figure).setOrigin((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_ORIGIN_IGNORED, (oldValue, newValue, figure) -> {
            ((ProgressBarFigure) figure).setOriginIgnored((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_FILL_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ProgressBarFigure) refreshableFigure;
            slider.setFillColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_FILLBACKGROUND_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ProgressBarFigure) refreshableFigure;
            slider.setFillBackgroundColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ProgressBarFigure) refreshableFigure;
            slider.setEffect3D((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_LABEL, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ProgressBarFigure) refreshableFigure;
            slider.setShowLabel((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_INDICATOR_MODE, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ProgressBarFigure) refreshableFigure;
            slider.setIndicatorMode((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ProgressBarFigure) refreshableFigure;
            slider.setHorizontal((Boolean) newValue);
            var model = (ProgressBarModel) getModel();

            if ((Boolean) newValue) {
                model.setLocation(model.getLocation().x - model.getSize().height / 2 + model.getSize().width / 2,
                        model.getLocation().y + model.getSize().height / 2 - model.getSize().width / 2);
            } else {
                model.setLocation(model.getLocation().x + model.getSize().width / 2 - model.getSize().height / 2,
                        model.getLocation().y - model.getSize().width / 2 + model.getSize().height / 2);
            }

            model.setSize(model.getSize().height, model.getSize().width);

            return false;
        });

        // enabled. WidgetBaseEditPart will force the widget as disabled in edit model,
        // which is not the case for the scaled slider
        setPropertyChangeHandler(PROP_ENABLED, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ProgressBarFigure) refreshableFigure;
            slider.setEnabled((Boolean) newValue);
            return false;
        });

        // Change fill color when "FillColor Alarm Sensitive" property changes.
        setPropertyChangeHandler(PROP_FILLCOLOR_ALARM_SENSITIVE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ProgressBarFigure) refreshableFigure;
            boolean sensitive = (Boolean) newValue;
            figure.setFillColor(delegate.calculateAlarmColor(sensitive, getWidgetModel().getFillColor()));
            return true;
        });

        // Change fill color when alarm severity changes.
        delegate.addAlarmSeverityListener((severity, figure) -> {
            if (!getWidgetModel().isFillColorAlarmSensitive()) {
                return false;
            }
            var progress = (ProgressBarFigure) figure;
            progress.setFillColor(delegate.calculateAlarmColor(getWidgetModel().isFillColorAlarmSensitive(),
                    getWidgetModel().getFillColor()));
            return true;
        });
    }
}
