/********************************************************************************
 * Copyright (c) 2012, 2021 Oak Ridge National Laboratory and others
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
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.visualparts.BorderStyle;

/**
 * Model for native text widget.
 *
 * @deprecated not used anymore. Only leave this here in case we need to reverse back.
 */
@Deprecated
public final class NativeTextModel extends TextInputModel {

    public static final String PROP_SHOW_NATIVE_BORDER = "show_native_border";

    public static final String PROP_PASSWORD_INPUT = "password_input";

    public static final String PROP_READ_ONLY = "read_only";

    public static final String PROP_SHOW_H_SCROLL = "show_h_scroll";

    public static final String PROP_SHOW_V_SCROLL = "show_v_scroll";

    public static final String PROP_NEXT_FOCUS = "next_focus";

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.NativeText";

    public NativeTextModel() {
        setSize(100, 25);
        setBorderStyle(BorderStyle.NONE);
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();
        addProperty(new BooleanProperty(PROP_SHOW_NATIVE_BORDER, "Show Native Border", WidgetPropertyCategory.Display,
                false));
        addProperty(new BooleanProperty(PROP_PASSWORD_INPUT, "Password Input", WidgetPropertyCategory.Behavior, false));
        addProperty(new BooleanProperty(PROP_READ_ONLY, "Read Only", WidgetPropertyCategory.Behavior, true));
        addProperty(new BooleanProperty(PROP_SHOW_H_SCROLL, "Show Horizontal Scrollbar", WidgetPropertyCategory.Display,
                false));
        addProperty(new BooleanProperty(PROP_SHOW_V_SCROLL, "Show Vertical Scrollbar", WidgetPropertyCategory.Display,
                false));
        addProperty(new ComboProperty(PROP_NEXT_FOCUS, "Next Focus", WidgetPropertyCategory.Behavior,
                FOCUS_TRAVERSE.stringValues(), 0));
        removeProperty(PROP_TRANSPARENT);
        removeProperty(PROP_ROTATION);
        removeProperty(PROP_ALIGN_V);
        removeProperty(PROP_DATETIME_FORMAT);
        removeProperty(PROP_SELECTOR_TYPE);
        removeProperty(PROP_FILE_RETURN_PART);
        removeProperty(PROP_FILE_SOURCE);
        removeProperty(PROP_SHOW_SCROLLBAR);
        // If border is alarm sensitive, redraw the border will also redraw the whole canvas in WebOPI
        // so make it invisible to make sure user can get best performance.
        // Maybe border is not frequently changed?
        // setPropertyVisible(PROP_BORDER_ALARMSENSITIVE, false);
    }

    @Override
    public boolean isShowNativeBorder() {
        return (Boolean) getPropertyValue(PROP_SHOW_NATIVE_BORDER);
    }

    @Override
    public boolean isReadOnly() {
        return (Boolean) getPropertyValue(PROP_READ_ONLY);
    }

    @Override
    public boolean isPasswordInput() {
        return (Boolean) getPropertyValue(PROP_PASSWORD_INPUT);
    }

    @Override
    public boolean isShowHScroll() {
        return (Boolean) getPropertyValue(PROP_SHOW_H_SCROLL);
    }

    @Override
    public boolean isShowVScroll() {
        return (Boolean) getPropertyValue(PROP_SHOW_V_SCROLL);
    }

    @Override
    public FOCUS_TRAVERSE getFocusTraverse() {
        return FOCUS_TRAVERSE.values()[(Integer) getPropertyValue(PROP_NEXT_FOCUS)];
    }

    @Override
    public String getTypeID() {
        return ID;
    }
}
