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
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.geometry.Point;
import org.yamcs.studio.data.FormatEnum;

/**
 * The model for text indicator.
 */
public class TextUpdateModel extends LabelModel {

    public static final String PROP_FORMAT_TYPE = "format_type";
    public static final String PROP_PRECISION = "precision";
    public static final String PROP_PRECISION_FROM_DB = "precision_from_pv";
    public static final String PROP_SHOW_UNITS = "show_units";
    public static final String PROP_SHOW_LOHI = "show_lohi";
    public static final String PROP_ROTATION = "rotation_angle";

    public TextUpdateModel() {
        setSize(100, 20);
        setForegroundColor(CustomMediaFactory.COLOR_BLACK);
        setTooltip("$(" + PROP_PVNAME + ")\n" + "$(" + PROP_PVVALUE + ")");
        setPropertyValue(PROP_WRAP_WORDS, false);
    }

    @Override
    public String getTypeID() {
        return "org.csstudio.opibuilder.widgets.TextUpdate";
    }

    @Override
    protected void configureProperties() {
        pvModel = true;
        super.configureProperties();
        WidgetPropertyCategory category = new WidgetPropertyCategory() {
            @Override
            public String toString() {
                return "Format";
            }
        };
        addProperty(new ComboProperty(PROP_FORMAT_TYPE, "Format Type", category, FormatEnum.stringValues(), 0));
        addProperty(new IntegerProperty(PROP_PRECISION, "Precision", category, 0, 0, 100));
        addProperty(new BooleanProperty(PROP_PRECISION_FROM_DB, "Precision from PV", category, true));
        addProperty(new BooleanProperty(PROP_SHOW_UNITS, "Show Units", category, true));
        addProperty(new BooleanProperty(PROP_SHOW_LOHI, "Show Low/High", category, true));
        addProperty(new DoubleProperty(PROP_ROTATION, "Rotation Angle", WidgetPropertyCategory.Display, 0, 0, 360));
        setPropertyValue(PROP_TEXT, "######");
        setPropertyValue(PROP_ALIGN_H, 0);
        setPropertyValue(PROP_ALIGN_V, 1);
        setPropertyVisible(PROP_SHOW_SCROLLBAR, false);
    }

    public FormatEnum getFormat() {
        return FormatEnum.values()[(Integer) getCastedPropertyValue(PROP_FORMAT_TYPE)];
    }

    public int getPrecision() {
        return (Integer) getCastedPropertyValue(PROP_PRECISION);
    }

    public boolean isPrecisionFromDB() {
        return (Boolean) getCastedPropertyValue(PROP_PRECISION_FROM_DB);
    }

    public boolean isShowUnits() {
        return (Boolean) getCastedPropertyValue(PROP_SHOW_UNITS);
    }

    public boolean isShowLoHi() {
        return (Boolean) getCastedPropertyValue(PROP_SHOW_LOHI);
    }

    /**
     * Returns the rotation angle for this widget.
     *
     * @return The rotation angle
     */
    public double getRotationAngle() {
        return (Double) getProperty(PROP_ROTATION).getPropertyValue();
    }

    public void setRotationAngle(double angle) {
        setPropertyValue(PROP_ROTATION, angle);
    }

    @Override
    public void rotate90(boolean clockwise) {
        super.rotate90(clockwise);
        if (clockwise) {
            setRotationAngle((getRotationAngle() + 90) % 360);
        } else {
            setRotationAngle((getRotationAngle() + 270) % 360);
        }
    }

    @Override
    public void rotate90(boolean clockwise, Point center) {
        super.rotate90(clockwise, center);
        if (clockwise) {
            setRotationAngle((getRotationAngle() + 90) % 360);
        } else {
            setRotationAngle((getRotationAngle() + 270) % 360);
        }
    }
}
