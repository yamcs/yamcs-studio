/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.introspection;

import org.eclipse.draw2d.Label;

/**
 * The introspector for widget inherited from {@link Label}.
 */
public class LabelWidgetIntrospector extends DefaultWidgetIntrospector {
    public static String[] LABEL_WIDGET_NON_PROPERTIES = new String[] { "icon", "iconAlignment", "iconTextGap",
            "labelAlignment", "textAlignment", "textPlacement" };

    @Override
    public String[] getNonProperties() {

        return concatenateStringArrays(super.getNonProperties(), LABEL_WIDGET_NON_PROPERTIES);
    }
}
