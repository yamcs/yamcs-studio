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
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.KnobModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.KnobModel.PROP_INCREMENT;
import static org.csstudio.opibuilder.widgets.model.KnobModel.PROP_KNOB_COLOR;
import static org.csstudio.opibuilder.widgets.model.KnobModel.PROP_RAMP_GRADIENT;
import static org.csstudio.opibuilder.widgets.model.KnobModel.PROP_SHOW_VALUE_LABEL;
import static org.csstudio.opibuilder.widgets.model.KnobModel.PROP_THUMB_COLOR;

import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.KnobModel;
import org.csstudio.swt.widgets.figures.KnobFigure;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the knob widget. The controller mediates between {@link KnobModel} and {@link KnobFigure}.
 */
public final class KnobEditPart extends AbstractMarkedWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = (KnobModel) getModel();

        var knob = new KnobFigure();

        initializeCommonFigureProperties(knob, model);

        knob.setBulbColor(model.getKnobColor());
        knob.setEffect3D(model.isEffect3D());
        knob.setThumbColor(model.getThumbColor());
        knob.setValueLabelVisibility(model.isShowValueLabel());
        knob.setGradient(model.isRampGradient());
        knob.setIncrement(model.getIncrement());

        knob.addManualValueChangeListener(newValue -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                setPVValue(PROP_PVNAME, newValue);
            }
        });

        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);

        return knob;
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_KNOB_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var knob = (KnobFigure) refreshableFigure;
            knob.setBulbColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_THUMB_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var knob = (KnobFigure) refreshableFigure;
            knob.setThumbColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var knob = (KnobFigure) refreshableFigure;
            knob.setEffect3D((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_VALUE_LABEL, (oldValue, newValue, refreshableFigure) -> {
            var knob = (KnobFigure) refreshableFigure;
            knob.setValueLabelVisibility((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_RAMP_GRADIENT, (oldValue, newValue, refreshableFigure) -> {
            var knob = (KnobFigure) refreshableFigure;
            knob.setGradient((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_INCREMENT, (oldValue, newValue, refreshableFigure) -> {
            var knob = (KnobFigure) refreshableFigure;
            knob.setIncrement((Double) newValue);
            return false;
        });

        // force square size
        IWidgetPropertyChangeHandler sizeHandler = (oldValue, newValue, figure) -> {
            if (((Integer) newValue) < KnobModel.MINIMUM_SIZE) {
                newValue = KnobModel.MINIMUM_SIZE;
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
