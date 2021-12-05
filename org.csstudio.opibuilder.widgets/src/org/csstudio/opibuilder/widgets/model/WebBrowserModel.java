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

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.eclipse.swt.graphics.RGB;

/**
 * The model for web browser widget.
 */
public class WebBrowserModel extends AbstractWidgetModel {

    public final String ID = "org.csstudio.opibuilder.widgets.webbrowser";
    public static final String PROP_URL = "url";
    public static final String PROP_SHOW_TOOLBAR = "show_toolbar";

    public WebBrowserModel() {
        setBorderStyle(BorderStyle.LINE);
        setBorderColor(new RGB(192, 192, 192));
        setSize(450, 300);
    }

    @Override
    protected void configureProperties() {
        addProperty(new StringProperty(
                PROP_URL, "URL", WidgetPropertyCategory.Basic, ""));
        addProperty(new BooleanProperty(PROP_SHOW_TOOLBAR, "Show Toolbar",
                WidgetPropertyCategory.Display, true));
        setPropertyVisible(PROP_FONT, false);
    }

    public String getURL() {
        return (String) getPropertyValue(PROP_URL);
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    public boolean isShowToolBar() {
        return (Boolean) getPropertyValue(PROP_SHOW_TOOLBAR);
    }

}
