/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.ActionsProperty;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.ui.util.CustomMediaFactory;

public final class MenuButtonModel extends AbstractPVWidgetModel implements ITextModel {
    private static final boolean DEFAULT_ACTIONS_FROM_PV = false;
    /**
     * The ID of the label property.
     */
    public static final String PROP_LABEL = "label";

    public static final String PROP_ACTIONS_FROM_PV = "actions_from_pv";

    public static final String PROP_TRANSPARENT = "transparent";

    /**
     * The ID of the show down arrow property.
     */
    public static final String PROP_SHOW_DOWN_ARROW = "show_down_arrow";

    /**
     * The default value of the height property.
     */
    private static final int DEFAULT_HEIGHT = 40;

    /**
     * The default value of the width property.
     */
    private static final int DEFAULT_WIDTH = 100;
    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.MenuButton";

    /**
     * Constructor.
     */
    public MenuButtonModel() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setBorderStyle(BorderStyle.BUTTON_RAISED);
        setForegroundColor(CustomMediaFactory.COLOR_BLACK);
        setPropertyValue(PROP_BORDER_ALARMSENSITIVE, false);
    }

    @Override
    protected void configureProperties() {
        addProperty(new StringProperty(PROP_LABEL, "Label", WidgetPropertyCategory.Display, ""));
        addProperty(new BooleanProperty(PROP_ACTIONS_FROM_PV, "Actions From PV", WidgetPropertyCategory.Behavior,
                DEFAULT_ACTIONS_FROM_PV));
        addProperty(new BooleanProperty(PROP_TRANSPARENT, "Transparent", WidgetPropertyCategory.Display, false));
        addProperty(
                new BooleanProperty(PROP_SHOW_DOWN_ARROW, "Show Down Arrow", WidgetPropertyCategory.Display, false));
        removeProperty(PROP_ACTIONS);
        addProperty(new ActionsProperty(PROP_ACTIONS, "Actions", WidgetPropertyCategory.Behavior, false));

        setPropertyVisible(PROP_ACTIONS, !DEFAULT_ACTIONS_FROM_PV);
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    /**
     * Return the label text.
     *
     * @return The label text.
     */
    public String getLabel() {
        return (String) getProperty(PROP_LABEL).getPropertyValue();
    }

    @Override
    public String getText() {
        return getLabel();
    }

    @Override
    public void setText(String text) {
        setPropertyValue(PROP_LABEL, text);
    }

    public boolean isActionsFromPV() {
        return (Boolean) getCastedPropertyValue(PROP_ACTIONS_FROM_PV);
    }

    public boolean isTransparent() {
        return (Boolean) getPropertyValue(PROP_TRANSPARENT);
    }

    public boolean showDownArrow() {
        return (Boolean) getPropertyValue(PROP_SHOW_DOWN_ARROW);
    }
}
