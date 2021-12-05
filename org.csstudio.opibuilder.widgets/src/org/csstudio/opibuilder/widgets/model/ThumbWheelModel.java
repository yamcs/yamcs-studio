/********************************************************************************
 * Copyright (c) 2007 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.OPIFont;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Model for the ThumbWheel.
 */
public class ThumbWheelModel extends AbstractPVWidgetModel {

    public static final String PROP_MIN = "minimum";

    public static final String PROP_MAX = "maximum";

    public static final String PROP_INTERNAL_FRAME_THICKNESS = "internalFrameSize";

    public static final String PROP_INTERNAL_FRAME_COLOR = "internalFrameColor";

    public static final String PROP_INTERNAL_FOCUSED_FRAME_COLOR = "focusedFrameColor";

    public static final String PROP_INTEGER_DIGITS_PART = "integerDigits";

    public static final String PROP_DECIMAL_DIGITS_PART = "decimalDigits";
    /** Load limit from PV. */
    public static final String PROP_LIMITS_FROM_PV = "limits_from_pv";

    public static final String PROP_SHOW_BUTTONS = "show_buttons";

    public static final String ID = "org.csstudio.opibuilder.widgets.ThumbWheel";

    // public static final String PROP_VALUE = "value";

    private static final int DEFAULT_HEIGHT = 60;

    /**
     * The default value of the width property.
     */
    private static final int DEFAULT_WIDTH = 120;

    /** The default value of the minimum property. */
    private static final double DEFAULT_MIN = 0;
    /** The default value of the maximum property. */
    private static final double DEFAULT_MAX = 100;

    /** The default value of the number of integer digits property. */
    private static final int DEFAULT_INTEGER_DIGITS = 3;

    /** The default value of the number of decimal digits property. */
    private static final int DEFAULT_DECIMAL_DIGITS = 2;

    public ThumbWheelModel() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setForegroundColor(new RGB(0, 0, 0));
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    @Override
    protected void configureProperties() {
        // addProperty(new DoubleProperty(PROP_VALUE, "Value",
        // WidgetPropertyCategory.Behavior, 0));
        addProperty(new DoubleProperty(PROP_MIN, "Minimum",
                WidgetPropertyCategory.Behavior, DEFAULT_MIN));
        addProperty(new DoubleProperty(PROP_MAX, "Maximum",
                WidgetPropertyCategory.Behavior, DEFAULT_MAX));
        addProperty(new IntegerProperty(PROP_INTEGER_DIGITS_PART,
                "Integer Digits", WidgetPropertyCategory.Behavior, DEFAULT_INTEGER_DIGITS));
        addProperty(new IntegerProperty(PROP_DECIMAL_DIGITS_PART,
                "Decimal Digits", WidgetPropertyCategory.Behavior, DEFAULT_DECIMAL_DIGITS));
        addProperty(new ColorProperty(PROP_INTERNAL_FRAME_COLOR,
                "Internal Frame Color", WidgetPropertyCategory.Display,
                ColorConstants.black.getRGB()));
        addProperty(new ColorProperty(PROP_INTERNAL_FOCUSED_FRAME_COLOR,
                "Focused Frame Color", WidgetPropertyCategory.Display,
                ColorConstants.blue.getRGB()));

        addProperty(new IntegerProperty(PROP_INTERNAL_FRAME_THICKNESS,
                "Internal Frame Thickness", WidgetPropertyCategory.Display, 1));
        addProperty(new BooleanProperty(PROP_LIMITS_FROM_PV, "Limits From PV",
                WidgetPropertyCategory.Behavior, false));

        addProperty(new BooleanProperty(PROP_SHOW_BUTTONS, "Show Buttons",
                WidgetPropertyCategory.Display, true));

    }

    public void setFont(OPIFont font) {
        setPropertyValue(PROP_FONT, font);
    }

    public int getWholePartDigits() {
        return (Integer) getProperty(PROP_INTEGER_DIGITS_PART).getPropertyValue();
    }

    public void setWholePartDigits(int val) {
        setPropertyValue(PROP_INTEGER_DIGITS_PART, val);
    }

    public int getDecimalPartDigits() {
        return (Integer) getProperty(PROP_DECIMAL_DIGITS_PART).getPropertyValue();
    }

    public void setDecimalPartDigits(int val) {
        setPropertyValue(PROP_DECIMAL_DIGITS_PART, val);
    }

    // public double getValue() {
    // return (Double)getProperty(PROP_VALUE).getPropertyValue();
    // }

    public int getInternalFrameThickness() {
        return (Integer) getProperty(PROP_INTERNAL_FRAME_THICKNESS).getPropertyValue();
    }

    public RGB getInternalFrameColor() {
        return getRGBFromColorProperty(PROP_INTERNAL_FRAME_COLOR);
    }

    public RGB getInternalFocusedFrameColor() {
        return getRGBFromColorProperty(PROP_INTERNAL_FOCUSED_FRAME_COLOR);
    }

    public double getMinimum() {
        return (Double) getProperty(PROP_MIN).getPropertyValue();
    }

    public double getMaximum() {
        return (Double) getProperty(PROP_MAX).getPropertyValue();
    }

    public Color getInternalBorderColor() {
        return getSWTColorFromColorProperty(PROP_INTERNAL_FRAME_COLOR);
    }

    public Color getInternalFocusedBorderColor() {
        return getSWTColorFromColorProperty(PROP_INTERNAL_FOCUSED_FRAME_COLOR);
    }

    public int getInternalBorderWidth() {
        return (Integer) getProperty(PROP_INTERNAL_FRAME_THICKNESS).getPropertyValue();
    }

    /**
     * @return true if limits will be load from DB, false otherwise
     */
    public boolean isLimitsFromPV() {
        return (Boolean) getProperty(PROP_LIMITS_FROM_PV).getPropertyValue();
    }

    public boolean isButtonVisible() {
        return (Boolean) getProperty(PROP_SHOW_BUTTONS).getPropertyValue();
    }
}
