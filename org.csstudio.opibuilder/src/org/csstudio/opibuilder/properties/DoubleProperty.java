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

import org.csstudio.opibuilder.properties.support.DoublePropertyDescriptor;
import org.csstudio.opibuilder.util.OPIColor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**
 * A property, which is able to handle Double values.
 */
public final class DoubleProperty extends AbstractWidgetProperty {

    /**
     * Lower border for the property value.
     */
    private double min;

    /**
     * Upper border for the property value.
     */
    private double max;

    /**
     * Double Property Constructor. The property value type is double.
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
    public DoubleProperty(String propId, String description, WidgetPropertyCategory category, double defaultValue) {
        super(propId, description, category, Double.valueOf(defaultValue));
        min = -Double.MAX_VALUE;
        max = Double.MAX_VALUE;
    }

    /**
     * Color Property Constructor. The property value type is {@link OPIColor}.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created.
     * @param min
     *            the minimum allowed double value.
     * @param max
     *            the maximum allowed double value.
     */
    public DoubleProperty(String propId, String description, WidgetPropertyCategory category, double defaultValue,
            double min, double max) {
        super(propId, description, category, Double.valueOf(defaultValue));
        this.min = min;
        this.max = max;
    }

    @Override
    public Object checkValue(Object value) {
        if (value == null) {
            return null;
        }

        Double acceptedValue = null;

        // check type
        if (!(value instanceof Double)) {
            if (value instanceof Number) {
                acceptedValue = ((Number) value).doubleValue();
            } else {
                try {
                    acceptedValue = Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    acceptedValue = null;
                }
            }
        } else {
            acceptedValue = (Double) value;
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
        return new DoublePropertyDescriptor(prop_id, description);
    }

    @Override
    public void writeToXML(Element propElement) {
        propElement.setText(getPropertyValue().toString());
    }

    @Override
    public Object readValueFromXML(Element propElement) {
        return Double.parseDouble(propElement.getValue());
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }
}
