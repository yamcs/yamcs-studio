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

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.swt.widgets.figures.TextFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.TextFigure.V_ALIGN;
import org.csstudio.ui.util.CustomMediaFactory;

/**
 * The model for label widget.
 */
public class LabelModel extends AbstractPVWidgetModel implements ITextModel {

    /**
     * The ID of the text property.
     */
    public static final String PROP_TEXT = "text";
    /** The ID of the <i>transparent</i> property. */
    public static final String PROP_TRANSPARENT = "transparent";

    /** The ID of the <i>Auto Size</i> property. */
    public static final String PROP_AUTOSIZE = "auto_size";

    public static final String PROP_ALIGN_H = "horizontal_alignment";
    public static final String PROP_ALIGN_V = "vertical_alignment";
    public static final String PROP_WRAP_WORDS = "wrap_words";
    public static final String PROP_SHOW_SCROLLBAR = "show_scrollbar";
    protected boolean pvModel = false;

    public LabelModel() {
        setBackgroundColor(CustomMediaFactory.COLOR_WHITE);
        setForegroundColor(CustomMediaFactory.COLOR_BLACK);
        setSize(80, 20);
    }

    @Override
    protected void configureProperties() {
        addProperty(new StringProperty(PROP_TEXT, "Text", WidgetPropertyCategory.Display, "Label", true));
        addProperty(new BooleanProperty(PROP_TRANSPARENT, "Transparent", WidgetPropertyCategory.Display, !pvModel));
        addProperty(new BooleanProperty(PROP_AUTOSIZE, "Auto Size", WidgetPropertyCategory.Display, false));
        addProperty(new ComboProperty(PROP_ALIGN_H, "Horizontal Alignment", WidgetPropertyCategory.Display,
                H_ALIGN.stringValues(), 1));
        addProperty(new ComboProperty(PROP_ALIGN_V, "Vertical Alignment", WidgetPropertyCategory.Display,
                V_ALIGN.stringValues(), 1));
        addProperty(new BooleanProperty(PROP_WRAP_WORDS, "Wrap Words", WidgetPropertyCategory.Behavior, false));
        addProperty(new BooleanProperty(PROP_SHOW_SCROLLBAR, "Show Scrollbar", WidgetPropertyCategory.Behavior, false));

        if (!pvModel) {
            setTooltip("");
            setPropertyVisible(PROP_PVNAME, false);
            setPropertyVisible(PROP_PVVALUE, false);
            setPropertyVisible(PROP_BACKCOLOR_ALARMSENSITIVE, false);
            setPropertyVisible(PROP_BORDER_ALARMSENSITIVE, false);
            setPropertyVisible(PROP_FORECOLOR_ALARMSENSITIVE, false);
            setPropertyVisible(PROP_ALARM_PULSING, false);
        }
    }

    public H_ALIGN getHorizontalAlignment() {
        return H_ALIGN.values()[(Integer) getCastedPropertyValue(PROP_ALIGN_H)];
    }

    public V_ALIGN getVerticalAlignment() {
        return V_ALIGN.values()[(Integer) getCastedPropertyValue(PROP_ALIGN_V)];
    }

    @Override
    public String getTypeID() {
        return "org.csstudio.opibuilder.widgets.Label";
    }

    @Override
    public String getText() {
        return (String) getCastedPropertyValue(PROP_TEXT);
    }

    @Override
    public void setText(String text) {
        setPropertyValue(PROP_TEXT, text);
    }

    public void setText(String text, boolean fire) {
        getProperty(PROP_TEXT).setPropertyValue(text, fire);
    }

    public boolean isTransparent() {
        return (Boolean) getCastedPropertyValue(PROP_TRANSPARENT);
    }

    public boolean isAutoSize() {
        return (Boolean) getCastedPropertyValue(PROP_AUTOSIZE);
    }

    public boolean isWrapWords() {
        return (Boolean) getPropertyValue(PROP_WRAP_WORDS);
    }

    public void setFont(OPIFont font) {
        setPropertyValue(PROP_FONT, font);
    }

    public boolean isShowScrollbar() {
        return (Boolean) getPropertyValue(PROP_SHOW_SCROLLBAR);
    }
}
