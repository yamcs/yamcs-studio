package org.csstudio.opibuilder.widgets.editparts;

import java.util.logging.Level;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.Activator;
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
        fig.setStartBit(((Integer) model.getPropertyValue(ByteMonitorModel.PROP_START_BIT)));
        fig.setNumBits(((Integer) model.getPropertyValue(ByteMonitorModel.PROP_NUM_BITS)));
        fig.setHorizontal(((Boolean) model.getPropertyValue(ByteMonitorModel.PROP_HORIZONTAL)));
        fig.setReverseBits(((Boolean) model.getPropertyValue(ByteMonitorModel.PROP_BIT_REVERSE)));
        fig.setPackedLEDs(((Boolean) model.getPropertyValue(ByteMonitorModel.PROP_PACK_LEDS)));
        fig.setLedBorderColor(
                ((OPIColor) model.getPropertyValue(ByteMonitorModel.PROP_LED_BORDER_COLOR)).getSWTColor());
        fig.setLedBorderWidth(((Integer) model.getPropertyValue(ByteMonitorModel.PROP_LED_BORDER)));
        fig.setSquareLED(((Boolean) model.getPropertyValue(ByteMonitorModel.PROP_SQUARE_LED)));
        fig.setOnColor(((OPIColor) model.getPropertyValue(ByteMonitorModel.PROP_ON_COLOR)).getSWTColor());
        fig.setOffColor(((OPIColor) model.getPropertyValue(ByteMonitorModel.PROP_OFF_COLOR)).getSWTColor());
        fig.setEffect3D((Boolean) getPropertyValue(ByteMonitorModel.PROP_EFFECT3D));
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

        removeAllPropertyChangeHandlers(AbstractWidgetModel.PROP_ENABLED);

        // enable
        IWidgetPropertyChangeHandler enableHandler = (oldValue, newValue, figure) -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                figure.setEnabled((Boolean) newValue);
            }
            return false;
        };
        setPropertyChangeHandler(AbstractWidgetModel.PROP_ENABLED, enableHandler);

        // PV_Value
        IWidgetPropertyChangeHandler pvhandler = (oldValue, newValue, refreshableFigure) -> {
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
                Activator.getLogger().log(Level.SEVERE,
                        NLS.bind("{0} does not accept non-numeric value.", getWidgetModel().getName()));
            }
            return false;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_PVVALUE, pvhandler);

        // on color
        IWidgetPropertyChangeHandler colorHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setOnColor(((OPIColor) newValue).getSWTColor());
            figure.drawValue();
            return true;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_ON_COLOR, colorHandler);

        // off color
        colorHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setOffColor(((OPIColor) newValue).getSWTColor());
            figure.drawValue();
            return true;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_OFF_COLOR, colorHandler);

        // change orientation of the bit display
        IWidgetPropertyChangeHandler horizontalHandler = (oldValue, newValue, refreshableFigure) -> {
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
        };

        setPropertyChangeHandler(ByteMonitorModel.PROP_HORIZONTAL, horizontalHandler);

        // change the display order of the bits
        IWidgetPropertyChangeHandler reverseBitsHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setReverseBits((Boolean) newValue);
            figure.drawValue();
            return true;
        };

        setPropertyChangeHandler(ByteMonitorModel.PROP_BIT_REVERSE, reverseBitsHandler);

        // Set the bit to use as a starting point
        IWidgetPropertyChangeHandler startBitHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setStartBit((Integer) newValue);
            figure.drawValue();
            return true;
        };

        setPropertyChangeHandler(ByteMonitorModel.PROP_START_BIT, startBitHandler);

        // Set the number of bits to display
        IWidgetPropertyChangeHandler numBitsHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setNumBits((Integer) newValue);
            figure.drawValue();
            return true;
        };

        setPropertyChangeHandler(ByteMonitorModel.PROP_NUM_BITS, numBitsHandler);

        // Square LED
        IWidgetPropertyChangeHandler squareLEDHandler = (oldValue, newValue, refreshableFigure) -> {
            var bm = (ByteMonitorFigure) refreshableFigure;
            bm.setSquareLED((Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_SQUARE_LED, squareLEDHandler);

        // LED spacing
        IWidgetPropertyChangeHandler ledBorderWidthHandler = (oldValue, newValue, refreshableFigure) -> {
            var bm = (ByteMonitorFigure) refreshableFigure;
            bm.setLedBorderWidth((int) newValue);
            return true;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_LED_BORDER, ledBorderWidthHandler);

        // LED border color
        IWidgetPropertyChangeHandler ledBorderColorHandler = (oldValue, newValue, refreshableFigure) -> {
            var bm = (ByteMonitorFigure) refreshableFigure;
            bm.setLedBorderColor(((OPIColor) newValue).getSWTColor());
            return true;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_LED_BORDER_COLOR, ledBorderColorHandler);

        // effect 3D
        IWidgetPropertyChangeHandler effect3DHandler = (oldValue, newValue, refreshableFigure) -> {
            var bmFig = (ByteMonitorFigure) refreshableFigure;
            bmFig.setEffect3D((Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_EFFECT3D, effect3DHandler);

        // labels
        IWidgetPropertyChangeHandler labelsHandler = (oldValue, newValue, refreshableFigure) -> {
            var bmFig = (ByteMonitorFigure) refreshableFigure;
            var model = getWidgetModel();
            bmFig.setLabels(model.getLabels());
            return true;
        };
        setPropertyChangeHandler(ByteMonitorModel.PROP_LABELS, labelsHandler);

        // Set the LED rendering style
        IWidgetPropertyChangeHandler packHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (ByteMonitorFigure) refreshableFigure;
            figure.setPackedLEDs((Boolean) newValue);
            figure.drawValue();
            return true;
        };

        setPropertyChangeHandler(ByteMonitorModel.PROP_PACK_LEDS, packHandler);
    }
}
