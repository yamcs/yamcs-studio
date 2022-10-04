/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.properties;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

public class ComboProperty extends AbstractWidgetProperty<Integer> {

    private String[] labelsArray;

    /**
     * Combo Property Constructor. The property value type is integer.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param labelsArray
     *            the array of labels in combo's drop box.
     * @param defaultValue
     *            the default value when the widget is first created.
     */
    public ComboProperty(String prop_id, String description, WidgetPropertyCategory category, String[] labelsArray,
            int defaultValue) {
        super(prop_id, description, category, Integer.valueOf(defaultValue));
        this.labelsArray = labelsArray;
    }

    @Override
    public Integer checkValue(Object value) {
        if (value == null) {
            return null;
        }
        Integer acceptedValue = null;

        // check type
        if (!(value instanceof Number)) {
            try {
                acceptedValue = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                acceptedValue = null;
            }
        } else {
            acceptedValue = ((Number) value).intValue();
        }

        // check range
        if (acceptedValue != null) {
            if (acceptedValue < 0) {
                acceptedValue = 0;
            } else if (acceptedValue >= labelsArray.length) {
                acceptedValue = labelsArray.length - 1;
            }
        }

        return acceptedValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new ComboBoxPropertyDescriptor(prop_id, description, labelsArray);
    }

    @Override
    public void writeToXML(Element propElement) {
        propElement.setText(getPropertyValue().toString());
    }

    @Override
    public Integer readValueFromXML(Element propElement) {
        try {
            return Integer.parseInt(propElement.getValue());
        } catch (NumberFormatException e) {
            return Boolean.parseBoolean(propElement.getValue()) ? 1 : 0;
        }
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }
}
