package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
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
        ThermometerModel model = getWidgetModel();

        ThermometerFigure thermometer = new ThermometerFigure();

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

        // fillColor
        IWidgetPropertyChangeHandler fillColorHandler = (oldValue, newValue, refreshableFigure) -> {
            ThermometerFigure thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setFillColor(((OPIColor) newValue).getSWTColor());
            return false;
        };
        setPropertyChangeHandler(ThermometerModel.PROP_FILL_COLOR, fillColorHandler);

        // fillBackgroundColor
        IWidgetPropertyChangeHandler fillBackColorHandler = (oldValue, newValue, refreshableFigure) -> {
            ThermometerFigure thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setFillBackgroundColor(((OPIColor) newValue).getSWTColor());
            return false;
        };
        setPropertyChangeHandler(ThermometerModel.PROP_FILLBACKGROUND_COLOR, fillBackColorHandler);

        // show bulb
        IWidgetPropertyChangeHandler showBulbHandler = (oldValue, newValue, refreshableFigure) -> {
            ThermometerFigure thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setShowBulb((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(ThermometerModel.PROP_SHOW_BULB, showBulbHandler);

        // unit
        IWidgetPropertyChangeHandler fahrenheitHandler = (oldValue, newValue, refreshableFigure) -> {
            ThermometerFigure thermometer = (ThermometerFigure) refreshableFigure;
            thermometer.setTemperatureUnit(TemperatureUnit.values()[(Integer) newValue]);
            return false;
        };
        setPropertyChangeHandler(ThermometerModel.PROP_UNIT, fahrenheitHandler);

        // effect 3D
        IWidgetPropertyChangeHandler effect3DHandler = (oldValue, newValue, refreshableFigure) -> {
            ThermometerFigure thermo = (ThermometerFigure) refreshableFigure;
            thermo.setEffect3D((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(ThermometerModel.PROP_EFFECT3D, effect3DHandler);

        // Change fill color when "FillColor Alarm Sensitive" property changes.
        IWidgetPropertyChangeHandler fillColorAlarmSensitiveHandler = (oldValue, newValue, refreshableFigure) -> {
            ThermometerFigure figure = (ThermometerFigure) refreshableFigure;
            boolean sensitive = (Boolean) newValue;
            figure.setFillColor(
                    delegate.calculateAlarmColor(sensitive,
                            getWidgetModel().getFillColor()));
            return true;
        };
        setPropertyChangeHandler(ThermometerModel.PROP_FILLCOLOR_ALARM_SENSITIVE, fillColorAlarmSensitiveHandler);

        // Change fill color when alarm severity changes.
        delegate.addAlarmSeverityListener((severity, figure) -> {
            if (!getWidgetModel().isFillColorAlarmSensitive()) {
                return false;
            }
            ThermometerFigure thermo = (ThermometerFigure) figure;
            thermo.setFillColor(
                    delegate.calculateAlarmColor(getWidgetModel().isFillColorAlarmSensitive(),
                            getWidgetModel().getFillColor()));
            return true;
        });
    }
}
