/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
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
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.swt.graphics.Color;

/**
 * The widget model of ellipse widget.
 */
public class EllipseModel extends AbstractShapeModel {

    public final String ID = "org.csstudio.opibuilder.widgets.Ellipse";

    /**
     * True if the ellipse should be filled with gradient effect.
     */
    public static final String PROP_GRADIENT = "gradient";

    /**
     * The color on background gradient start.
     */
    public static final String PROP_BACKGROUND_GRADIENT_START_COLOR = "bg_gradient_color";

    /**
     * The color on foreground gradient start.
     */
    public static final String PROP_FOREGROUND_GRADIENT_START_COLOR = "fg_gradient_color";

    public EllipseModel() {
        setScaleOptions(true, true, true);
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();
        addProperty(new ColorProperty(PROP_BACKGROUND_GRADIENT_START_COLOR, "Background Gradient Start Color",
                WidgetPropertyCategory.Display, CustomMediaFactory.COLOR_WHITE));
        addProperty(new ColorProperty(PROP_FOREGROUND_GRADIENT_START_COLOR, "Foreground Gradient Start Color",
                WidgetPropertyCategory.Display, CustomMediaFactory.COLOR_WHITE));
        addProperty(new BooleanProperty(PROP_GRADIENT, "Gradient", WidgetPropertyCategory.Display, false));
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    public boolean isGradient() {
        return (Boolean) getPropertyValue(PROP_GRADIENT);
    }

    public Color getBackgroundGradientStartColor() {
        return ((OPIColor) getPropertyValue(PROP_BACKGROUND_GRADIENT_START_COLOR)).getSWTColor();
    }

    public Color getForegroundGradientStartColor() {
        return ((OPIColor) getPropertyValue(PROP_FOREGROUND_GRADIENT_START_COLOR)).getSWTColor();
    }
}
