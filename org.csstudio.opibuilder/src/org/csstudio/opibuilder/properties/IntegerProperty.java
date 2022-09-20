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

import org.csstudio.opibuilder.properties.support.IntegerPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

/**
 * The integer property.
 */
public class IntegerProperty extends AbstractWidgetProperty<Integer> {

    /**
     * Lower border for the property value.
     */
    private int min;

    /**
     * Upper border for the property value.
     */
    private int max;

    /**
     * Integer Property Constructor. The property value type is integer.
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
    public IntegerProperty(String prop_id, String description, WidgetPropertyCategory category, int defaultValue) {
        super(prop_id, description, category, Integer.valueOf(defaultValue));
        min = Integer.MIN_VALUE;
        max = Integer.MAX_VALUE;
    }

    /**
     * Integer Property Constructor. The property value type is integer.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created.
     * @param minValue
     *            the minimum allowed integer value.
     * @param maxValue
     *            the maximum allowed integer value.
     */
    public IntegerProperty(String prop_id, String description, WidgetPropertyCategory category, int defaultValue,
            int minValue, int maxValue) {
        super(prop_id, description, category, Integer.valueOf(defaultValue));
        assert minValue < maxValue;
        min = minValue;
        max = maxValue;
    }

    @Override
    public Integer checkValue(Object value) {
        if (value == null) {
            return null;
        }

        Integer acceptedValue = null;

        // check type
        if (!(value instanceof Integer)) {
            if (value instanceof Number) {
                acceptedValue = ((Number) value).intValue();
            } else {
                try {
                    acceptedValue = Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    acceptedValue = null;
                }
            }
        } else {
            acceptedValue = (Integer) value;
        }

        // check borders
        if (acceptedValue != null) {
            if (acceptedValue > max) {
                acceptedValue = max;
            } else if (acceptedValue < min) {
                acceptedValue = min;
            }
        }

        return acceptedValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new IntegerPropertyDescriptor(prop_id, description);
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
            return Integer.valueOf((int) Double.parseDouble(propElement.getValue()));
        }
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }
}
