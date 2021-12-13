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

import org.csstudio.opibuilder.properties.support.BooleanPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**
 * A boolean widget property.
 */
public final class BooleanProperty extends AbstractWidgetProperty<Boolean> {

    /**
     * Boolean Property Constructor
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created.
     */
    public BooleanProperty(String propId, String description, WidgetPropertyCategory category, boolean defaultValue) {
        super(propId, description, category, Boolean.valueOf(defaultValue));
    }

    @Override
    public Boolean checkValue(Object value) {
        if (value == null) {
            return null;
        }

        Boolean acceptedValue;
        if (value instanceof Boolean) {
            acceptedValue = (Boolean) value;
        } else {
            acceptedValue = Boolean.parseBoolean(value.toString());
        }

        return acceptedValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new BooleanPropertyDescriptor(prop_id, description);
    }

    @Override
    public void writeToXML(Element propElement) {
        propElement.setText(getPropertyValue().toString());
    }

    @Override
    public Boolean readValueFromXML(Element propElement) {
        return Boolean.parseBoolean(propElement.getValue());
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }

    @Override
    public String toStringInRuleScript(Boolean propValue) {
        return propValue ? "true" : "false";
    }
}
