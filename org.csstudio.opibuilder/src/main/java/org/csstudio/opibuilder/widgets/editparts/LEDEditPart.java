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
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_OFF_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_OFF_LABEL;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_ON_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_ON_LABEL;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_BULB_BORDER;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_BULB_BORDER_COLOR;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_NSTATES;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_SQUARE_LED;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_STATE_COLOR;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_STATE_FALLBACK_COLOR;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_STATE_FALLBACK_LABEL;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_STATE_LABEL;
import static org.csstudio.opibuilder.widgets.model.LEDModel.PROP_STATE_VALUE;

import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel;
import org.csstudio.opibuilder.widgets.model.LEDModel;
import org.csstudio.swt.widgets.figures.AbstractBoolFigure;
import org.csstudio.swt.widgets.figures.LEDFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

/**
 * LED EditPart
 */
public class LEDEditPart extends AbstractBoolEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var led = new LEDFigure();

        initializeCommonFigureProperties(led, model);
        led.setEffect3D(model.isEffect3D());
        led.setSquareLED(model.isSquareLED());
        return led;
    }

    @Override
    public LEDModel getWidgetModel() {
        return (LEDModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var led = (LEDFigure) refreshableFigure;
            led.setEffect3D((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_SQUARE_LED, (oldValue, newValue, refreshableFigure) -> {
            var led = (LEDFigure) refreshableFigure;
            led.setSquareLED((Boolean) newValue);
            if (!(Boolean) newValue) {
                var width = Math.min(getWidgetModel().getWidth(), getWidgetModel().getHeight());
                getWidgetModel().setSize(width, width);
            }
            return true;
        });

        // force square size
        IWidgetPropertyChangeHandler sizeHandler = (oldValue, newValue, figure) -> {
            if (getWidgetModel().isSquareLED()) {
                return false;
            }
            if (((Integer) newValue) < LEDModel.MINIMUM_SIZE) {
                newValue = LEDModel.MINIMUM_SIZE;
            }
            getWidgetModel().setSize((Integer) newValue, (Integer) newValue);
            return false;
        };
        PropertyChangeListener sizeListener = evt -> sizeHandler.handleChange(evt.getOldValue(), evt.getNewValue(),
                getFigure());
        getWidgetModel().getProperty(PROP_WIDTH).addPropertyChangeListener(sizeListener);
        getWidgetModel().getProperty(PROP_HEIGHT).addPropertyChangeListener(sizeListener);

        getWidgetModel().getProperty(PROP_NSTATES).addPropertyChangeListener(
                evt -> initializeNStatesProperties((Integer) evt.getOldValue(), (Integer) evt.getNewValue(),
                        (LEDFigure) getFigure(), getWidgetModel()));

        getWidgetModel().getProperty(PROP_STATE_FALLBACK_LABEL)
                .addPropertyChangeListener(
                        evt -> initializeStateFallbackLabel((String) evt.getOldValue(), (String) evt.getNewValue(),
                                (LEDFigure) getFigure(), getWidgetModel()));

        getWidgetModel().getProperty(PROP_STATE_FALLBACK_COLOR)
                .addPropertyChangeListener(
                        evt -> initializeStateFallbackColor(((OPIColor) evt.getOldValue()).getSWTColor(),
                                ((OPIColor) evt.getNewValue()).getSWTColor(), (LEDFigure) getFigure(),
                                getWidgetModel()));

        getWidgetModel().getProperty(PROP_BULB_BORDER)
                .addPropertyChangeListener(evt -> initializeStateBulbBorderWidth((Integer) evt.getNewValue(),
                        (LEDFigure) getFigure(), getWidgetModel()));

        getWidgetModel().getProperty(PROP_BULB_BORDER_COLOR)
                .addPropertyChangeListener(
                        evt -> initializeStateBulbBorderColor(((OPIColor) evt.getNewValue()).getSWTColor(),
                                (LEDFigure) getFigure(), getWidgetModel()));

        for (var idx = 0; idx < LEDFigure.MAX_NSTATES; idx++) {
            var state = idx;
            getWidgetModel().getProperty(String.format(PROP_STATE_LABEL, state))
                    .addPropertyChangeListener(
                            evt -> initializeStateLabel(state, (String) evt.getOldValue(), (String) evt.getNewValue(),
                                    (LEDFigure) getFigure(), getWidgetModel()));
            getWidgetModel().getProperty(String.format(PROP_STATE_COLOR, state))
                    .addPropertyChangeListener(
                            evt -> initializeStateColor(state, ((OPIColor) evt.getOldValue()).getSWTColor(),
                                    ((OPIColor) evt.getNewValue()).getSWTColor(), (LEDFigure) getFigure(),
                                    getWidgetModel()));
            getWidgetModel().getProperty(String.format(PROP_STATE_VALUE, state))
                    .addPropertyChangeListener(
                            evt -> initializeStateValue(state, (Double) evt.getOldValue(), (Double) evt.getNewValue(),
                                    (LEDFigure) getFigure(), getWidgetModel()));
        }
    }

    @Override
    protected void initializeCommonFigureProperties(AbstractBoolFigure abstractFigure,
            AbstractBoolWidgetModel abstractModel) {

        super.initializeCommonFigureProperties(abstractFigure, abstractModel);

        var model = (LEDModel) abstractModel;
        var figure = (LEDFigure) abstractFigure;

        initializeStateBulbBorderColor(model.getBulbBorderColor(), figure, model);
        initializeStateBulbBorderWidth(model.getBulbBorderWidth(), figure, model);

        initializeNStatesProperties(LEDFigure.MAX_NSTATES, model.getNStates(), figure, model);
        initializeStateFallbackLabel(null, model.getStateFallbackLabel(), figure, model);
        initializeStateFallbackColor(null, model.getStateFallbackColor(), figure, model);
        for (var state = 0; state < LEDFigure.MAX_NSTATES; state++) {
            initializeStateColor(state, null, model.getStateColor(state), figure, model);
            initializeStateLabel(state, null, model.getStateLabel(state), figure, model);
            initializeStateValue(state, 0.0, model.getStateValue(state), figure, model);
        }
    }

    protected void initializeNStatesProperties(int oldNStates, int newNStates, LEDFigure figure, LEDModel model) {
        if (newNStates <= 2) {
            model.setPropertyVisible(PROP_ON_COLOR, true);
            model.setPropertyVisible(PROP_ON_LABEL, true);
            model.setPropertyVisible(PROP_OFF_COLOR, true);
            model.setPropertyVisible(PROP_OFF_LABEL, true);
            model.setPropertyVisibleAndSavable(PROP_NSTATES, true, false);
            model.setPropertyVisibleAndSavable(PROP_STATE_FALLBACK_COLOR, false, false);
            model.setPropertyVisibleAndSavable(PROP_STATE_FALLBACK_LABEL, false, false);
            for (var idx = 0; idx < oldNStates; idx++) {
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_COLOR, idx), false, false);
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_LABEL, idx), false, false);
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_VALUE, idx), false, false);
            }
        } else if (newNStates > 2) {
            model.setPropertyVisible(PROP_ON_COLOR, false);
            model.setPropertyVisible(PROP_ON_LABEL, false);
            model.setPropertyVisible(PROP_OFF_COLOR, false);
            model.setPropertyVisible(PROP_OFF_LABEL, false);
            model.setPropertyVisibleAndSavable(PROP_NSTATES, true, true);
            model.setPropertyVisibleAndSavable(PROP_STATE_FALLBACK_COLOR, true, true);
            model.setPropertyVisibleAndSavable(PROP_STATE_FALLBACK_LABEL, true, true);
            for (var idx = 0; idx < newNStates; idx++) {
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_COLOR, idx), true, true);
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_LABEL, idx), true, true);
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_VALUE, idx), true, true);
            }
            for (var idx = newNStates; idx < oldNStates; idx++) {
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_COLOR, idx), false, false);
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_LABEL, idx), false, false);
                model.setPropertyVisibleAndSavable(String.format(PROP_STATE_VALUE, idx), false, false);
            }
        }
        figure.setNStates(newNStates);
    }

    protected void initializeStateFallbackLabel(String oldLabel, String newLabel, LEDFigure figure, LEDModel model) {
        figure.setStateFallbackLabel(newLabel);
    }

    protected void initializeStateFallbackColor(Color oldColor, Color newColor, LEDFigure figure, LEDModel model) {
        figure.setStateFallbackColor(newColor);
    }

    protected void initializeStateLabel(int state, String oldLabel, String newLabel, LEDFigure figure, LEDModel model) {
        figure.setStateLabel(state, newLabel);
    }

    protected void initializeStateColor(int state, Color oldColor, Color newColor, LEDFigure figure, LEDModel model) {
        figure.setStateColor(state, newColor);
    }

    protected void initializeStateValue(int state, double oldValue, double newValue, LEDFigure figure, LEDModel model) {
        figure.setStateValue(state, newValue);
    }

    protected void initializeStateBulbBorderWidth(int newWidth, LEDFigure figure, LEDModel model) {
        figure.setBulbBorderWidth(newWidth);
    }

    protected void initializeStateBulbBorderColor(Color newColor, LEDFigure figure, LEDModel model) {
        figure.setBulbBorderColor(newColor);
    }
}
