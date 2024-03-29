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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.Figure;

/**
 * The default widget introspector, which will filter out the non widget properties from {@link Figure}.
 */
public class DefaultWidgetIntrospector {
    public static String[] FIGURE_NON_PROPERTIES = new String[] { "children", "class", "clientArea", "coordinateSystem",
            "clippingStrategy", "focusTraversable", "insets", "layoutManager", "localBackgroundColor",
            "localForegroundColor", "maximumSize", "minimumSize", "mirrored", "parent", "preferredSize",
            "requestFocusEnabled", "toolTip", "showing", "updateManager", "valid", "beanInfo" };

    public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
        Introspector.flushFromCaches(beanClass);
        var bi = Introspector.getBeanInfo(beanClass);
        var bd = bi.getBeanDescriptor();
        var mds = bi.getMethodDescriptors();
        var esds = bi.getEventSetDescriptors();
        var pds = bi.getPropertyDescriptors();

        var filteredPDList = new ArrayList<PropertyDescriptor>();

        List<String> nonPropList = Arrays.asList(getNonProperties());
        for (var pd : pds) {
            if (!nonPropList.contains(pd.getName()) && pd.getWriteMethod() != null && pd.getReadMethod() != null) {
                filteredPDList.add(pd);
            }
        }

        var defaultEvent = bi.getDefaultEventIndex();
        var defaultProperty = bi.getDefaultPropertyIndex();

        return new GenericBeanInfo(bd, esds, defaultEvent,
                filteredPDList.toArray(new PropertyDescriptor[filteredPDList.size()]), defaultProperty, mds, null);
    }

    public String[] getNonProperties() {
        return FIGURE_NON_PROPERTIES;
    }

    public String[] concatenateStringArrays(String[] A, String[] B) {
        var C = new String[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }
}
