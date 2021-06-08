/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import java.net.URL;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.eclipse.ui.PlatformUI;

/**
 * The action that opens webpage in default system web browser.
 * 
 * @author Xihui Chen
 *
 */
public class OpenWebpageAction extends AbstractWidgetAction {

    public static final String PROP_HYPERLINK = "hyperlink";

    @Override
    protected void configureProperties() {
        addProperty(new StringProperty(PROP_HYPERLINK, "Web Address", WidgetPropertyCategory.Basic, "http://"));

    }

    @Override
    public ActionType getActionType() {
        return ActionType.OPEN_WEBPAGE;
    }

    @Override
    public void run() {
        String hyperLink = getHyperLink();
        if (!hyperLink.contains("://")) {
            hyperLink = "http://" + hyperLink;
        }
        try {
            PlatformUI.getWorkbench().getBrowserSupport()
                    .createBrowser("opi_web_browser").openURL(new URL(hyperLink));
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, "Failed to open " + hyperLink, e);
        }
    }

    private String getHyperLink() {
        return (String) getPropertyValue(PROP_HYPERLINK);
    }

    @Override
    public String getDefaultDescription() {
        return super.getDefaultDescription() + " " + getHyperLink();
    }
}
