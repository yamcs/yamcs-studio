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
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_BIT_REVERSE;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_EFFECT3D;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_HORIZONTAL;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_LABELS;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_LED_BORDER;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_LED_BORDER_COLOR;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_NUM_BITS;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_OFF_COLOR;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_ON_COLOR;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_PACK_LEDS;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_SQUARE_LED;
import static org.csstudio.opibuilder.widgets.model.ByteMonitorModel.PROP_START_BIT;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.ByteMonitorModel;
import org.csstudio.swt.widgets.figures.ByteMonitorFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.osgi.util.NLS;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VType;

/**
 * This class implements the widget edit part for the Byte Monitor widget. This displays the bits in a value as s series
 * of LEDs
 */
public class ByteMonitorEditPart extends AbstractPVWidgetEditPart {

    @Override
    public Object getValue() {
        return ((ByteMonitorFigure) getFigure()).getValue();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Integer) {
            ((ByteMonitorFigure) getFigure()).setValue((Integer) value);
        } else if (value instanceof Long) {
            ((ByteMonitorFigure) getFigure()).setValue((Long) value);
        } else if (value instanceof Double) {
            ((ByteMonitorFigure) getFigure()).setValue((Double) value);
        } else if (value instanceof Number) {
            ((ByteMonitorFigure) getFigure()).setValue(((Number) value).longValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    protected IFigure doCreateFigure() {
        var model = (ByteMonitorModel) getWidgetModel();

        var fig = new ByteMonitorFigure();
        setModel(model);
        setFigure(fig);
        fig.setStartBit(((Integer) model.getPropertyValue(PROP_START_BIT)));
        fig.setNumBits(((Integer) model.getPropertyValue(PROP_NUM_BITS)));
        fig.setHorizontal(((Boolean) model.getPropertyValue(PROP_HORIZONTAL)));
        fig.setReverseBits(((Boolean) model.getPropertyValue(PROP_BIT_REVERSE)));
        fig.setPackedLEDs(((Boolean) model.getPropertyValue(PROP_PACK_LEDS)));
        fig.setLedBorderColor(((OPIColor) model.getPropertyValue(PROP_LED_BORDER_COLOR)).getSWTColor());
        fig.setLedBorderWidth(((Integer) model.getPropertyValue(PROP_LED_BORDER)));
        fig.setSquareLED(((Boolean) model.getPropertyValue(PROP_SQUARE_LED)));
        fig.setOnColor(((OPIColor) model.getPropertyValue(PROP_ON_COLOR)).getSWTColor());
        fig.setOffColor(((OPIColor) model.getPropertyValue(PROP_OFF_COLOR)).getSWTColor());
        fig.setEffect3D((Boolean) getPropertyValue(PROP_EFFECT3D));
        fig.setLabels(model.getLabels());
        fig.setValue(0x1111);
        fig.drawValue();

        return fig;
    }

    @Override
    public ByteMonitorModel getWidgetModel() {
        return (ByteMonitorModel) super.getWidgetModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerBasePropertyChangeHandlers();
        getFigure().setEnabled(getWidgetModel().isEnabled() && (getExecutionMode() == ExecutionMode.RUN_MODE));

        removeAllPropertyChangeHandlers(PROP_ENABLED);

        setPropertyChangeHandler(PROP_ENABLED, (oldValue, newValue, figure) -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                figure.setEnabled((Boolean) newValue);
            }
            return false;
        });

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            var succeed = true;
            if ((newValue != null) && (newValue instanceof VType)) {
                var number = VTypeHelper.getNumber(((VType) newValue));
                if (number != null) {
                    setValue(number);
                } else {
                    succeed = false;
                }

            } else {
                succeed = false;
            }
            if (!succeed) {
                setValue(0);
                OPIBuilderPlugin.getLogger().log(Level.SEVERE,
                        NLS.bind("{0} does not accept non-numeric value.", getWidgetModel().getName()));
            }
            return false;
        });

        setPropertyChangeHandler(PROP_ON_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setOnColor(((OPIColor) newValue).getSWTColor());
            figure.drawValue();
            return true;
        });

        setPropertyChangeHandler(PROP_OFF_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setOffColor(((OPIColor) newValue).getSWTColor());
            figure.drawValue();
            return true;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setHorizontal((Boolean) newValue);
            var model = getWidgetModel();
            if ((Boolean) newValue) {
                model.setLocation(model.getLocation().x - model.getSize().height / 2 + model.getSize().width / 2,
                        model.getLocation().y + model.getSize().height / 2 - model.getSize().width / 2);
            } else {
                model.setLocation(model.getLocation().x + model.getSize().width / 2 - model.getSize().height / 2,
                        model.getLocation().y - model.getSize().width / 2 + model.getSize().height / 2);
            }

            model.setSize(model.getSize().height, model.getSize().width);

            figure.drawValue();
            return true;
        });

        setPropertyChangeHandler(PROP_BIT_REVERSE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setReverseBits((Boolean) newValue);
            figure.drawValue();
            return true;
        });

        setPropertyChangeHandler(PROP_START_BIT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setStartBit((Integer) newValue);
            figure.drawValue();
            return true;
        });

        setPropertyChangeHandler(PROP_NUM_BITS, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setNumBits((Integer) newValue);
            figure.drawValue();
            return true;
        });

        setPropertyChangeHandler(PROP_SQUARE_LED, (oldValue, newValue, refreshableFigure) -> {
            var bm = (ByteMonitorFigure) refreshableFigure;
            bm.setSquareLED((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LED_BORDER, (oldValue, newValue, refreshableFigure) -> {
            var bm = (ByteMonitorFigure) refreshableFigure;
            bm.setLedBorderWidth((int) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LED_BORDER_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var bm = (ByteMonitorFigure) refreshableFigure;
            bm.setLedBorderColor(((OPIColor) newValue).getSWTColor());
            return true;
        });

        setPropertyChangeHandler(PROP_EFFECT3D, (oldValue, newValue, refreshableFigure) -> {
            var bmFig = (ByteMonitorFigure) refreshableFigure;
            bmFig.setEffect3D((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LABELS, (oldValue, newValue, refreshableFigure) -> {
            var bmFig = (ByteMonitorFigure) refreshableFigure;
            var model = getWidgetModel();
            bmFig.setLabels(model.getLabels());
            return true;
        });

        setPropertyChangeHandler(PROP_PACK_LEDS, (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setPackedLEDs((Boolean) newValue);
            figure.drawValue();
            return true;
        });
    }
}
