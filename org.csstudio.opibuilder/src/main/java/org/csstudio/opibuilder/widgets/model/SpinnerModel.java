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
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.swt.widgets.figures.SpinnerFigure.NumericFormatType;

/**
 * The model of spinner widget.
 */
public class SpinnerModel extends LabelModel {

    public final String ID = "org.csstudio.opibuilder.widgets.spinner";

    /** The ID of the minimum property. */
    public static final String PROP_MIN = "minimum";

    /** The ID of the maximum property. */
    public static final String PROP_MAX = "maximum";

    /**
     * the amount the scrollbar will move when the up or down arrow buttons are pressed.
     */
    public static final String PROP_STEP_INCREMENT = "step_increment";

    /**
     * The amount the scrollbar will move when the page up or page down areas are pressed.
     */
    public static final String PROP_PAGE_INCREMENT = "page_increment";

    public static final String PROP_LIMITS_FROM_PV = "limits_from_pv";
    public static final String PROP_PRECISION = "precision";
    public static final String PROP_PRECISION_FROM_PV = "precision_from_pv";
    public static final String PROP_BUTTONS_ON_LEFT = "buttons_on_left";

    public static final String PROP_HORIZONTAL_BUTTONS_LAYOUT = "horizontal_buttons_layout";

    public static final String PROP_SHOW_TEXT = "show_text";

    /**
     * The Format of the value.
     */
    public static final String PROP_FORMAT = "format";

    /** The default value of the minimum property. */
    private static double DEFAULT_MIN = Double.NEGATIVE_INFINITY;

    /** The default value of the maximum property. */
    private static double DEFAULT_MAX = Double.POSITIVE_INFINITY;

    private static double DEFAULT_STEP_INCREMENT = 1;

    private static double DEFAULT_PAGE_INCREMENT = 10;

    public SpinnerModel() {
        setSize(85, 25);
        setBorderStyle(BorderStyle.LOWERED);
    }

    @Override
    protected void configureProperties() {
        pvModel = true;
        super.configureProperties();
        removeProperty(PROP_AUTOSIZE);
        removeProperty(PROP_SHOW_SCROLLBAR);
        removeProperty(PROP_WRAP_WORDS);
        setPropertyVisible(PROP_TEXT, false);
        addProperty(new DoubleProperty(PROP_MIN, "Minimum", WidgetPropertyCategory.Behavior, DEFAULT_MIN));

        addProperty(new DoubleProperty(PROP_MAX, "Maximum", WidgetPropertyCategory.Behavior, DEFAULT_MAX));

        addProperty(new DoubleProperty(PROP_STEP_INCREMENT, "Step Increment", WidgetPropertyCategory.Behavior,
                DEFAULT_STEP_INCREMENT), true);

        addProperty(new DoubleProperty(PROP_PAGE_INCREMENT, "Page Increment", WidgetPropertyCategory.Behavior,
                DEFAULT_PAGE_INCREMENT), true);

        addProperty(new BooleanProperty(PROP_LIMITS_FROM_PV, "Limits from PV", WidgetPropertyCategory.Behavior, true));

        addProperty(new ComboProperty(PROP_FORMAT, "Format", WidgetPropertyCategory.Display,
                NumericFormatType.stringValues(), 0));

        addProperty(new IntegerProperty(PROP_PRECISION, "Precision", WidgetPropertyCategory.Display, 3, 0, 100));
        addProperty(new BooleanProperty(PROP_PRECISION_FROM_PV, "Precision from PV", WidgetPropertyCategory.Display,
                false));
        addProperty(
                new BooleanProperty(PROP_BUTTONS_ON_LEFT, "Buttons on Left", WidgetPropertyCategory.Display, false));

        addProperty(new BooleanProperty(PROP_HORIZONTAL_BUTTONS_LAYOUT, "Horizontal Buttons Layout",
                WidgetPropertyCategory.Display, false));

        addProperty(new BooleanProperty(PROP_SHOW_TEXT, "Show text", WidgetPropertyCategory.Display, true));
    }

    public Double getMinimum() {
        return (Double) getProperty(PROP_MIN).getPropertyValue();
    }

    public Double getMaximum() {
        return (Double) getProperty(PROP_MAX).getPropertyValue();
    }

    public boolean showText() {
        return (Boolean) getPropertyValue(PROP_SHOW_TEXT);
    }

    public Double getStepIncrement() {
        return (Double) getProperty(PROP_STEP_INCREMENT).getPropertyValue();
    }

    public double getPageIncrement() {
        return (Double) getProperty(PROP_PAGE_INCREMENT).getPropertyValue();
    }

    public boolean isLimitsFromPV() {
        return (Boolean) getProperty(PROP_LIMITS_FROM_PV).getPropertyValue();
    }

    public boolean isPrecisionFromPV() {
        return (Boolean) getProperty(PROP_PRECISION_FROM_PV).getPropertyValue();
    }

    public boolean isButtonsOnLeft() {
        return (Boolean) getPropertyValue(PROP_BUTTONS_ON_LEFT);
    }

    public boolean isHorizontalButtonsLayout() {
        return (Boolean) getPropertyValue(PROP_HORIZONTAL_BUTTONS_LAYOUT);
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    public NumericFormatType getFormat() {
        int i = (Integer) getPropertyValue(PROP_FORMAT);
        return NumericFormatType.values()[i];
    }
}
