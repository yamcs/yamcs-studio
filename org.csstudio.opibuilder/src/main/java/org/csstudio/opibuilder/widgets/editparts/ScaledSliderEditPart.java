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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ENABLED;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.ScaledSliderModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.ScaledSliderModel.PROP_FILLBACKGROUND_COLOR;
import static org.csstudio.opibuilder.widgets.model.ScaledSliderModel.PROP_FILL_COLOR;
import static org.csstudio.opibuilder.widgets.model.ScaledSliderModel.PROP_HORIZONTAL;
import static org.csstudio.opibuilder.widgets.model.ScaledSliderModel.PROP_PAGE_INCREMENT;
import static org.csstudio.opibuilder.widgets.model.ScaledSliderModel.PROP_STEP_INCREMENT;
import static org.csstudio.opibuilder.widgets.model.ScaledSliderModel.PROP_THUMB_COLOR;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.ScaledSliderModel;
import org.csstudio.swt.widgets.figures.ScaledSliderFigure;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the scaled slider widget. The controller mediates between {@link ScaledSliderModel} and
 * {@link ScaledSliderFigure}.
 */
public final class ScaledSliderEditPart extends AbstractMarkedWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var slider = new ScaledSliderFigure();

        initializeCommonFigureProperties(slider, model);
        slider.setFillColor(model.getFillColor());
        slider.setEffect3D(model.isEffect3D());
        slider.setFillBackgroundColor(model.getFillbackgroundColor());
        slider.setThumbColor(model.getThumbColor());
        slider.setHorizontal(model.isHorizontal());
        slider.setStepIncrement(model.getStepIncrement());
        slider.setPageIncrement(model.getPageIncrement());
        slider.addManualValueChangeListener(newValue -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                setPVValue(PROP_PVNAME, newValue);
            }
        });

        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);
        return slider;
    }

    @Override
    public ScaledSliderModel getWidgetModel() {
        return (ScaledSliderModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_FILL_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setFillColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_FILLBACKGROUND_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setFillBackgroundColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_THUMB_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setThumbColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setEffect3D((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setHorizontal((Boolean) newValue);
            var model = (ScaledSliderModel) getModel();

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
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setEnabled((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_STEP_INCREMENT, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setStepIncrement((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_PAGE_INCREMENT, (oldValue, newValue, refreshableFigure) -> {
            var slider = (ScaledSliderFigure) refreshableFigure;
            slider.setPageIncrement((Double) newValue);
            return false;
        });
    }
}
