/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties;

import java.util.List;

import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**
 * The widget property for list. This property is only used for property change communication between model and
 * editpart, so it is not savable and viewable in property sheet.
 */
public class UnsavableListProperty extends AbstractWidgetProperty<List<?>> {

    /**
     * String Property Constructor. The property value type is {@link List}.
     * 
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. Can be NULL.
     */
    public UnsavableListProperty(String prop_id, String description, WidgetPropertyCategory category,
            List<?> defaultValue) {
        super(prop_id, description, category, defaultValue);
    }

    @Override
    public List<?> checkValue(Object value) {
        if (value instanceof List) {
            return (List<?>) value;
        }
        return null;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return null;
    }

    @Override
    public void writeToXML(Element propElement) {
        /* NOP */ }

    @Override
    public List<?> readValueFromXML(Element propElement) {
        return null;
    }

    @Override
    public boolean isSavable() {
        return false;
    }
}
