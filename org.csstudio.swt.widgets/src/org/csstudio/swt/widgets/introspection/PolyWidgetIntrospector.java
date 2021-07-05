/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.swt.widgets.introspection;

import org.eclipse.draw2d.AbstractPointListShape;

/**The introspector for widget inherited from {@link AbstractPointListShape}.
 * @author Xihui Chen
 *
 */
public class PolyWidgetIntrospector extends ShapeWidgetIntrospector {
    public static String[] POLY_WIDGET_NON_PROPERTIES = new String[]{
        "start",
        "end"
    };
    @Override
    public String[] getNonProperties() {

        return concatenateStringArrays(super.getNonProperties(), POLY_WIDGET_NON_PROPERTIES);
    }

}
