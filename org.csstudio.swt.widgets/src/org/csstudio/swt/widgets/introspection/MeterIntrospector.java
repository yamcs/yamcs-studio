/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.introspection;

import org.csstudio.swt.widgets.figures.AbstractScaledWidgetFigure;

/**
 * The introspector for widget inherited from {@link AbstractScaledWidgetFigure}.
 */
public class MeterIntrospector extends ScaleWidgetIntrospector {
    public static String[] METER_WIDGET_NON_PROPERTIES = new String[] {
            "transparent"
    };

    @Override
    public String[] getNonProperties() {

        return concatenateStringArrays(super.getNonProperties(), METER_WIDGET_NON_PROPERTIES);
    }

}
