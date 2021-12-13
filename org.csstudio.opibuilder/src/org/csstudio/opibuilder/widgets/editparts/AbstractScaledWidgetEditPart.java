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

import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_LOG_SCALE;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_MAJOR_TICK_STEP_HINT;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_MAX;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_MIN;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_SCALE_FONT;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_SCALE_FORMAT;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_SHOW_MINOR_TICKS;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_SHOW_SCALE;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_VALUE_LABEL_FORMAT;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel;
import org.csstudio.swt.widgets.figures.AbstractScaledWidgetFigure;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VType;

/**
 * Base editPart controller for a widget based on {@link AbstractScaledWidgetModel}.
 */
public abstract class AbstractScaledWidgetEditPart extends AbstractPVWidgetEditPart {

    /**
     * Sets those properties on the figure that are defined in the {@link AbstractScaledWidgetFigure} base class. This
     * method is provided for the convenience of subclasses, which can call this method in their implementation of
     */
    protected void initializeCommonFigureProperties(AbstractScaledWidgetFigure figure,
            AbstractScaledWidgetModel model) {

        figure.setRange(model.getMinimum(), model.getMaximum());
        figure.setValue((model.getMinimum() + model.getMaximum()) / 2);
        figure.setMajorTickMarkStepHint(model.getMajorTickStepHint());
        figure.setLogScale(model.isLogScaleEnabled());
        figure.setShowScale(model.isShowScale());
        figure.setShowMinorTicks(model.isShowMinorTicks());
        figure.setTransparent(model.isTransparent());
        figure.getScale().setFont(model.getScaleFont().getSWTFont());
        setScaleFormat(figure, model.getScaleFormat());
        setValueLabelFormat(figure, model.getValueLabelFormat());
    }

    /**
     * Registers property change handlers for the properties defined in {@link AbstractScaledWidgetModel}. This method
     * is provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link #registerPropertyChangeHandlers()}.
     */
    protected void registerCommonPropertyChangeHandlers() {
        setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if (newValue == null) {
                return false;
            }
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setValue(VTypeHelper.getDouble((VType) newValue));
            return false;
        });

        setPropertyChangeHandler(PROP_MIN, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setRange((Double) newValue, ((AbstractScaledWidgetModel) getModel()).getMaximum());
            return false;
        });

        setPropertyChangeHandler(PROP_MAX, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setRange(((AbstractScaledWidgetModel) getModel()).getMinimum(), (Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_MAJOR_TICK_STEP_HINT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setMajorTickMarkStepHint((Integer) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_LOG_SCALE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setLogScale((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_SCALE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setShowScale((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_MINOR_TICKS, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setShowMinorTicks((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.setTransparent((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SCALE_FONT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractScaledWidgetFigure) refreshableFigure;
            figure.getScale().setFont(((OPIFont) newValue).getSWTFont());
            return false;
        });

        setPropertyChangeHandler(PROP_SCALE_FORMAT, (oldValue, newValue, figure) -> {
            var scaleFigure = (AbstractScaledWidgetFigure) figure;
            setScaleFormat(scaleFigure, (String) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_VALUE_LABEL_FORMAT, (oldValue, newValue, figure) -> {
            var scaleFigure = (AbstractScaledWidgetFigure) figure;
            setValueLabelFormat(scaleFigure, (String) newValue);
            return false;
        });
    }

    private void setScaleFormat(AbstractScaledWidgetFigure scaleFigure, String numericFormat) {
        var scale = scaleFigure.getScale();
        if (numericFormat.trim().equals("")) {
            scale.setAutoFormat(true);
        } else {
            try {
                scale.setAutoFormat(false);
                scale.setFormatPattern(numericFormat);
            } catch (Exception e) {
                OPIBuilderPlugin.getLogger().log(Level.SEVERE,
                        numericFormat + " is not a valid numeric format. The scale will be auto formatted.");
                scale.setAutoFormat(true);
            }
        }
        // update value label
        scaleFigure.setValue(scaleFigure.getValue());
    }

    private void setValueLabelFormat(AbstractScaledWidgetFigure scaleFigure, String valueLabelFormat) {
        try {
            scaleFigure.setValueLabelFormat(valueLabelFormat);
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, valueLabelFormat + " is not a valid numeric format."
                    + " The value label will be formatted in the same way as scale.");
            scaleFigure.setValueLabelFormat("");
        }
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((AbstractScaledWidgetFigure) getFigure()).setValue(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Double getValue() {
        return ((AbstractScaledWidgetFigure) getFigure()).getValue();
    }
}
