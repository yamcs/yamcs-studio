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

import org.csstudio.opibuilder.properties.support.PointlistPropertyDescriptor;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**
 * The property for script.
 */
public class PointListProperty extends AbstractWidgetProperty<PointList> {

    /**
     * XML ELEMENT name <code>POINT</code>.
     */
    public static final String XML_ELEMENT_POINT = "point";

    /**
     * XML ATTRIBUTE name <code>X</code>.
     */
    public static final String XML_ATTRIBUTE_X = "x";

    /**
     * XML ATTRIBUTE name <code>Y</code>.
     */
    public static final String XML_ATTRIBUTE_Y = "y";

    /**
     * PointList Property Constructor. The property value type is {@link PointList}.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. cannot be null.
     */
    public PointListProperty(String prop_id, String description, WidgetPropertyCategory category,
            PointList defaultValue) {
        super(prop_id, description, category, defaultValue);
    }

    @Override
    public PointList checkValue(Object value) {
        if (value == null) {
            return new PointList();
        }
        PointList acceptableValue = null;
        if (value instanceof PointList) {
            acceptableValue = (PointList) value;
        } else if (value instanceof int[]) {
            acceptableValue = new PointList((int[]) value);
        }
        return acceptableValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new PointlistPropertyDescriptor(prop_id, description);
    }

    @Override
    public PointList readValueFromXML(Element propElement) {
        var result = new PointList();
        for (var oe : propElement.getChildren(XML_ELEMENT_POINT)) {
            var se = (Element) oe;
            result.addPoint(Integer.parseInt(se.getAttributeValue(XML_ATTRIBUTE_X)),
                    Integer.parseInt(se.getAttributeValue(XML_ATTRIBUTE_Y)));
        }
        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        var size = getPropertyValue().size();
        for (var i = 0; i < size; i++) {
            var point = getPropertyValue().getPoint(i);
            var pointElement = new Element(XML_ELEMENT_POINT);
            pointElement.setAttribute(XML_ATTRIBUTE_X, "" + point.x);
            pointElement.setAttribute(XML_ATTRIBUTE_Y, "" + point.y);
            propElement.addContent(pointElement);
        }
    }
}
