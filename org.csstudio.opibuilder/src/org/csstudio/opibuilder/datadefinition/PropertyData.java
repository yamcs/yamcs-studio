/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.datadefinition;

import org.csstudio.opibuilder.properties.AbstractWidgetProperty;

/**
 * Hold place for temp property value.
 */
public class PropertyData {
    public AbstractWidgetProperty property;
    public Object tmpValue;

    public PropertyData(AbstractWidgetProperty property, Object value) {
        this.property = property;
        this.tmpValue = value;
    }

}
