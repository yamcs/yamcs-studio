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

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.StringListProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ByteMonitorModel extends AbstractPVWidgetModel {

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.bytemonitor";

    /** The number of bits to display */
    public static final String PROP_NUM_BITS = "numBits";

    /** The bit number to start displaying */
    public static final String PROP_START_BIT = "startBit";

    /** True if the LEDs are horizontal arranged. */
    public static final String PROP_HORIZONTAL = "horizontal";

    /** Reverse the direction that bytes are displayed normal display is start bit on right or bottom */
    public static final String PROP_BIT_REVERSE = "bitReverse";

    /** Default color if the bit is on */
    public static final String PROP_ON_COLOR = "on_color";

    /** Default color if the bit is off */
    public static final String PROP_OFF_COLOR = "off_color";

    /** True if the LEDs are square LED. */
    public static final String PROP_SQUARE_LED = "square_led";

    /** The default color of the on color property. */
    private static final RGB DEFAULT_ON_COLOR = new RGB(0, 255, 0);
    /** The default color of the off color property. */
    private static final RGB DEFAULT_OFF_COLOR = new RGB(0, 100, 0);

    /** The ID of the effect 3D property. */
    public static final String PROP_EFFECT3D = "effect_3d";

    /** Label of each bit */
    public static final String PROP_LABELS = "label";

    /** Spacing between LEDs */
    public static final String PROP_LED_BORDER = "led_border";
    /** Color of space between LEDs */
    public static final String PROP_LED_BORDER_COLOR = "led_border_color";

    public static final String PROP_PACK_LEDS = "led_packed";

    public static final Integer DEFAULT_LED_BORDER = 3;
    public static final Color DEFAULT_LED_BORDER_COLOR = CustomMediaFactory.getInstance()
            .getColor(CustomMediaFactory.COLOR_DARK_GRAY);

    public ByteMonitorModel() {
        setSize(292, 20);
    }

    @Override
    protected void configureProperties() {
        addProperty(new IntegerProperty(PROP_NUM_BITS, "Number of Bits", WidgetPropertyCategory.Display, 16, 0, 64));
        addProperty(new IntegerProperty(PROP_START_BIT, "Start Bit", WidgetPropertyCategory.Display, 0, 0, 64));
        addProperty(new BooleanProperty(PROP_HORIZONTAL, "Horizontal", WidgetPropertyCategory.Display, true));
        addProperty(new BooleanProperty(PROP_BIT_REVERSE, "Reverse Bits", WidgetPropertyCategory.Display, false));
        addProperty(new ColorProperty(PROP_ON_COLOR, "On Color", WidgetPropertyCategory.Display, DEFAULT_ON_COLOR));
        addProperty(new ColorProperty(PROP_OFF_COLOR, "Off Color", WidgetPropertyCategory.Display, DEFAULT_OFF_COLOR));
        addProperty(new BooleanProperty(PROP_SQUARE_LED, "Square LED", WidgetPropertyCategory.Display, false));
        addProperty(new BooleanProperty(PROP_EFFECT3D, "3D Effect", WidgetPropertyCategory.Display, true));
        addProperty(
                new StringListProperty(PROP_LABELS, "Labels", WidgetPropertyCategory.Display, new ArrayList<String>()));
        addProperty(
                new IntegerProperty(PROP_LED_BORDER, "LED border", WidgetPropertyCategory.Display, DEFAULT_LED_BORDER));
        addProperty(new ColorProperty(PROP_LED_BORDER_COLOR, "LED border color", WidgetPropertyCategory.Display,
                DEFAULT_LED_BORDER_COLOR.getRGB()));
        addProperty(new BooleanProperty(PROP_PACK_LEDS, "Pack LEDs", WidgetPropertyCategory.Display, false));
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    public boolean isHorizontal() {
        return (Boolean) getPropertyValue(PROP_HORIZONTAL);
    }

    public boolean isReverseBits() {
        return (Boolean) getPropertyValue(PROP_BIT_REVERSE);
    }

    @Override
    public void flipHorizontally() {
        super.flipHorizontally();
        if (isHorizontal()) {
            setPropertyValue(PROP_BIT_REVERSE, !isReverseBits());
        }
    }

    @Override
    public void flipHorizontally(int centerX) {
        super.flipHorizontally(centerX);
        if (isHorizontal()) {
            setPropertyValue(PROP_BIT_REVERSE, !isReverseBits());
        }
    }

    @Override
    public void flipVertically() {
        super.flipVertically();
        if (!isHorizontal()) {
            setPropertyValue(PROP_BIT_REVERSE, !isReverseBits());
        }
    }

    @Override
    public void flipVertically(int centerY) {
        super.flipVertically(centerY);
        if (!isHorizontal()) {
            setPropertyValue(PROP_BIT_REVERSE, !isReverseBits());
        }
    }

    @Override
    public void rotate90(boolean clockwise) {
        setPropertyValue(PROP_HORIZONTAL, !isHorizontal());
    }

    @Override
    public void rotate90(boolean clockwise, Point center) {
        super.rotate90(clockwise, center);
        setPropertyValue(PROP_HORIZONTAL, !isHorizontal());
        super.rotate90(true);
    }

    @SuppressWarnings("unchecked")
    public List<String> getLabels() {
        return (List<String>) getPropertyValue(PROP_LABELS);
    }
}
