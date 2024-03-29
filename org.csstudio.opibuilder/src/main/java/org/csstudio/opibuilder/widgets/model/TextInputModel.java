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

import java.util.Arrays;

import org.csstudio.opibuilder.model.IWidgetInfoProvider;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.opibuilder.widgets.model.ActionButtonModel.Style;
import org.csstudio.swt.widgets.figures.TextInputFigure.FileReturnPart;
import org.csstudio.swt.widgets.figures.TextInputFigure.FileSource;
import org.csstudio.swt.widgets.figures.TextInputFigure.SelectorType;
import org.osgi.framework.Version;

/**
 * The model for text input.
 */
public class TextInputModel extends TextUpdateModel {

    /**
     * Version less than this has no style property.
     */
    private static final Version VERSION_BEFORE_STYLE = new Version(3, 1, 5);

    public enum FOCUS_TRAVERSE {
        LOSE("Lose Focus"), KEEP("Keep Focus"), NEXT("Next Widget"), PREVIOUS("Previous Widget");

        private String description;

        FOCUS_TRAVERSE(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            var result = new String[values().length];
            var i = 0;
            for (var f : values()) {
                result[i++] = f.toString();
            }
            return result;
        }
    }

    /** The minimum input value allowed. */
    public static final String PROP_MIN = "minimum";

    /** The maximum input value allowed. */
    public static final String PROP_MAX = "maximum";

    /** Load limit from PV. */
    public static final String PROP_LIMITS_FROM_PV = "limits_from_pv";

    /** Load limit from PV. */
    public static final String PROP_DATETIME_FORMAT = "datetime_format";

    /** Load limit from PV. */
    public static final String PROP_SELECTOR_TYPE = "selector_type";

    /** Load limit from PV. */
    public static final String PROP_FILE_SOURCE = "file_source";

    /** Load limit from PV. */
    public static final String PROP_FILE_RETURN_PART = "file_return_part";

    /** Allow multi-line input. */
    public static final String PROP_MULTILINE_INPUT = "multiline_input";

    /** The default value of the minimum property. */
    private static double DEFAULT_MIN = Double.NEGATIVE_INFINITY;

    /** The default value of the maximum property. */
    private static double DEFAULT_MAX = Double.POSITIVE_INFINITY;

    /** The message which will be shown on confirm dialog. */
    public static final String PROP_CONFIRM_MESSAGE = "confirm_message";

    // Properties for native text
    public static final String PROP_SHOW_NATIVE_BORDER = "show_native_border";

    public static final String PROP_PASSWORD_INPUT = "password_input";

    public static final String PROP_READ_ONLY = "read_only";

    public static final String PROP_SHOW_H_SCROLL = "show_h_scroll";

    public static final String PROP_SHOW_V_SCROLL = "show_v_scroll";

    public static final String PROP_NEXT_FOCUS = "next_focus";

    public static final String PROP_STYLE = "style";

    public TextInputModel() {
        setSize(100, 25);
        setPropertyValue(PROP_LIMITS_FROM_PV, false);
    }

    @Override
    public String getTypeID() {
        return "org.csstudio.opibuilder.widgets.TextInput";
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();

        addProperty(new ComboProperty(PROP_STYLE, "Style", WidgetPropertyCategory.Basic, Style.stringValues(),
                Style.CLASSIC.ordinal()));

        addProperty(new DoubleProperty(PROP_MIN, "Minimum", WidgetPropertyCategory.Behavior, DEFAULT_MIN));

        addProperty(new DoubleProperty(PROP_MAX, "Maximum", WidgetPropertyCategory.Behavior, DEFAULT_MAX));

        addProperty(new BooleanProperty(PROP_LIMITS_FROM_PV, "Limits From PV", WidgetPropertyCategory.Behavior, true));

        addProperty(
                new BooleanProperty(PROP_MULTILINE_INPUT, "Multi-line Input", WidgetPropertyCategory.Behavior, false));

        addProperty(new StringProperty(PROP_DATETIME_FORMAT, "Datetime Format", WidgetPropertyCategory.Display,
                "yyyy-MM-dd HH:mm:ss"));
        addProperty(new ComboProperty(PROP_SELECTOR_TYPE, "Selector Type", WidgetPropertyCategory.Display,
                SelectorType.stringValues(), SelectorType.NONE.ordinal()));
        addProperty(new ComboProperty(PROP_FILE_SOURCE, "File Source", WidgetPropertyCategory.Display,
                FileSource.stringValues(), FileSource.WORKSPACE.ordinal()));
        addProperty(new ComboProperty(PROP_FILE_RETURN_PART, "File Return Part", WidgetPropertyCategory.Display,
                FileReturnPart.stringValues(), FileReturnPart.FULL_PATH.ordinal()));

        addProperty(
                new StringProperty(PROP_CONFIRM_MESSAGE, "Confirm Message", WidgetPropertyCategory.Behavior, "", true));

        addProperty(new BooleanProperty(PROP_SHOW_NATIVE_BORDER, "Show Native Border", WidgetPropertyCategory.Display,
                true));
        addProperty(new BooleanProperty(PROP_PASSWORD_INPUT, "Password Input", WidgetPropertyCategory.Behavior, false));
        addProperty(new BooleanProperty(PROP_READ_ONLY, "Read Only", WidgetPropertyCategory.Behavior, false));
        addProperty(new BooleanProperty(PROP_SHOW_H_SCROLL, "Show Horizontal Scrollbar", WidgetPropertyCategory.Display,
                false));
        addProperty(new BooleanProperty(PROP_SHOW_V_SCROLL, "Show Vertical Scrollbar", WidgetPropertyCategory.Display,
                false));
        addProperty(new ComboProperty(PROP_NEXT_FOCUS, "Next Focus", WidgetPropertyCategory.Behavior,
                FOCUS_TRAVERSE.stringValues(), 0));

        setPropertyVisible(PROP_DATETIME_FORMAT, false);
        setPropertyVisible(PROP_FILE_RETURN_PART, false);
        setPropertyVisible(PROP_FILE_SOURCE, false);
        setPropertyVisible(PROP_WRAP_WORDS, false);
        setPropertyVisible(PROP_ALIGN_V, false);

        setText("");
        setPropertyValue(PROP_BORDER_STYLE, BorderStyle.LOWERED.ordinal());
        setPropertyValue(PROP_BORDER_ALARMSENSITIVE, false);
    }

    @Override
    public Version getVersion() {
        return new Version(2, 0, 0);
    }

    @Override
    public void processVersionDifference(Version boyVersionOnFile) {
        super.processVersionDifference(boyVersionOnFile);
        if (boyVersionOnFile.compareTo(VERSION_BEFORE_STYLE) < 0) {
            if (getWidgetType().equals("Text") || getWidgetType().equals("Native Text")) {
                setPropertyValue(PROP_WIDGET_TYPE, "Text Input");
                setPropertyValue(PROP_STYLE, Style.NATIVE.ordinal());
            }
        }
    }

    /**
     * @return the minimum value
     */
    public Double getMinimum() {
        return (Double) getProperty(PROP_MIN).getPropertyValue();
    }

    /**
     * @return the maximum value
     */
    public Double getMaximum() {
        return (Double) getProperty(PROP_MAX).getPropertyValue();
    }

    /**
     * @return true if limits will be load from DB, false otherwise
     */
    public boolean isLimitsFromPV() {
        return (Boolean) getProperty(PROP_LIMITS_FROM_PV).getPropertyValue();
    }

    public String getDateTimeFormat() {
        return (String) getPropertyValue(PROP_DATETIME_FORMAT);
    }

    public SelectorType getSelectorType() {
        return SelectorType.values()[(Integer) getPropertyValue(PROP_SELECTOR_TYPE)];
    }

    public FileSource getFileSource() {
        return FileSource.values()[(Integer) getPropertyValue(PROP_FILE_SOURCE)];
    }

    public FileReturnPart getFileReturnPart() {
        return FileReturnPart.values()[(Integer) getPropertyValue(PROP_FILE_RETURN_PART)];
    }

    public boolean isMultilineInput() {
        return (Boolean) getPropertyValue(PROP_MULTILINE_INPUT);
    }

    public String getConfirmMessage() {
        return (String) getPropertyValue(PROP_CONFIRM_MESSAGE);
    }

    public boolean isShowNativeBorder() {
        return (Boolean) getPropertyValue(PROP_SHOW_NATIVE_BORDER);
    }

    public boolean isReadOnly() {
        return (Boolean) getPropertyValue(PROP_READ_ONLY);
    }

    public boolean isPasswordInput() {
        return (Boolean) getPropertyValue(PROP_PASSWORD_INPUT);
    }

    public boolean isShowHScroll() {
        return (Boolean) getPropertyValue(PROP_SHOW_H_SCROLL);
    }

    public boolean isShowVScroll() {
        return (Boolean) getPropertyValue(PROP_SHOW_V_SCROLL);
    }

    public FOCUS_TRAVERSE getFocusTraverse() {
        return FOCUS_TRAVERSE.values()[(Integer) getPropertyValue(PROP_NEXT_FOCUS)];
    }

    public Style getStyle() {
        return Style.values()[(Integer) getProperty(PROP_STYLE).getPropertyValue()];
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IWidgetInfoProvider.class) {
            return adapter.cast((IWidgetInfoProvider) key -> {
                // get the propID that should be unique when it is put in an array
                if (key.equals(ArrayModel.ARRAY_UNIQUEPROP_ID)) {
                    return Arrays.asList(PROP_TEXT);
                }
                return null;
            });
        }
        return super.getAdapter(adapter);
    }
}
