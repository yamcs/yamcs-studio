/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.script;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.util.WidgetsService;
import org.eclipse.osgi.util.NLS;

/**
 * The Utility Class to help managing widgets.
 */
public class WidgetUtil {

    /**
     * Create a new widget model with the give widget type ID.
     * 
     * @param widgetTypeID
     *            type ID of the widget. You can get the typeID of a widget by opening an OPI with this widget in text
     *            editor.
     * @return the widget model.
     * @throws Exception
     *             if the widget type ID does not exist.
     */
    public static AbstractWidgetModel createWidgetModel(String widgetTypeID) throws Exception {
        var widgetDescriptor = WidgetsService.getInstance().getWidgetDescriptor(widgetTypeID);
        if (widgetDescriptor != null) {
            return widgetDescriptor.getWidgetModel();
        } else {
            throw new RuntimeException(NLS.bind("The widget type ID: {0} does not exist!", widgetTypeID));
        }
    }

}
