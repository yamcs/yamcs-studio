/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.swt.widgets.figures.ThermometerFigure.TemperatureUnit;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * This class defines a thermometer widget model.
 */
public class ThermometerModel extends AbstractMarkedWidgetModel {

    /** The ID of the fill color property. */
    public static final String PROP_FILL_COLOR = "fill_color";

    /** The ID of the fillcolor alarm sensitive property. */
    public static final String PROP_FILLCOLOR_ALARM_SENSITIVE = "fillcolor_alarm_sensitive";

    /** The ID of the show minor ticks property. */
    public static final String PROP_SHOW_BULB = "show_bulb";

    /** The ID of the fahrenheit property. */
    public static final String PROP_UNIT = "unit";

    /** The ID of the effect 3D property. */
    public static final String PROP_EFFECT3D = "effect_3d";

    /**
     * The ID of the fillbackground-Color property.
     */
    public static final String PROP_FILLBACKGROUND_COLOR = "color_fillbackground";

    /** The default value of the default fill color property. */
    private static final RGB DEFAULT_FILL_COLOR = new RGB(255, 0, 0);

    /** The default value of the height property. */
    private static final int DEFAULT_HEIGHT = 200;

    /** The default value of the width property. */
    private static final int DEFAULT_WIDTH = 100;

    /**
     * The default value of the fillbackground color property.
     */
    private static final RGB DEFAULT_FILLBACKGROUND_COLOR = new RGB(210, 210, 210);

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.thermometer";

    public ThermometerModel() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setForegroundColor(new RGB(0, 0, 0));
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();
        addProperty(
                new ColorProperty(PROP_FILL_COLOR, "Fill Color", WidgetPropertyCategory.Display, DEFAULT_FILL_COLOR));

        addProperty(new BooleanProperty(PROP_FILLCOLOR_ALARM_SENSITIVE, "FillColor Alarm Sensitive",
                WidgetPropertyCategory.Display, false));

        addProperty(new BooleanProperty(PROP_SHOW_BULB, "Show Bulb", WidgetPropertyCategory.Display, true));

        addProperty(new ComboProperty(PROP_UNIT, "Unit", WidgetPropertyCategory.Display, TemperatureUnit.stringValues(),
                0));

        addProperty(new ColorProperty(PROP_FILLBACKGROUND_COLOR, "Color Fillbackground", WidgetPropertyCategory.Display,
                DEFAULT_FILLBACKGROUND_COLOR));

        addProperty(new BooleanProperty(PROP_EFFECT3D, "3D Effect", WidgetPropertyCategory.Display, true));

        setPropertyValue(PROP_LO_COLOR, new RGB(255, 128, 0));
        setPropertyValue(PROP_HI_COLOR, new RGB(255, 128, 0));
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    /**
     * @return the fill color
     */
    public Color getFillColor() {
        return getSWTColorFromColorProperty(PROP_FILL_COLOR);
    }

    /**
     * @return true if the fill color is sensitive to alarm
     */
    public boolean isFillColorAlarmSensitive() {
        return (Boolean) getProperty(PROP_FILLCOLOR_ALARM_SENSITIVE).getPropertyValue();
    }

    /**
     * @return true if the bulb should be shown, false otherwise
     */
    public boolean isShowBulb() {
        return (Boolean) getProperty(PROP_SHOW_BULB).getPropertyValue();
    }

    /**
     * @return true if unit is in fahrenheit, false otherwise
     */
    public TemperatureUnit getUnit() {
        return TemperatureUnit.values()[(Integer) getProperty(PROP_UNIT).getPropertyValue()];
    }

    /**
     * @return true if the widget would be painted with 3D effect, false otherwise
     */
    public boolean isEffect3D() {
        return (Boolean) getProperty(PROP_EFFECT3D).getPropertyValue();
    }

    /**
     * Gets the RGB for fillbackground.
     *
     * @return The fillbackground color
     */
    public Color getFillbackgroundColor() {
        return getSWTColorFromColorProperty(PROP_FILLBACKGROUND_COLOR);
    }
}
