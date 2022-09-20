/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.properties;

import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

public class MatrixProperty extends AbstractWidgetProperty<double[][]> {

    /**
     * XML ELEMENT name for a row.
     */
    public static final String XML_ELEMENT_ROW = "row";

    /**
     * XML ELEMENT name for a column.
     */
    public static final String XML_ELEMENT_COLUMN = "col";

    public MatrixProperty(String prop_id, String description, WidgetPropertyCategory category,
            double[][] defaultValue) {
        super(prop_id, description, category, defaultValue);
    }

    @Override
    public double[][] checkValue(Object value) {
        if (value == null) {
            return null;
        }
        double[][] acceptableValue = null;
        if (value instanceof double[][]) {
            acceptableValue = (double[][]) value;
        }
        return acceptableValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return null;
    }

    @Override
    public void writeToXML(Element propElement) {
        for (var row : propertyValue) {
            var rowElement = new Element(XML_ELEMENT_ROW);
            for (var e : row) {
                var colElement = new Element(XML_ELEMENT_COLUMN);
                colElement.setText(Double.toString(e));
                rowElement.addContent(colElement);
            }
            propElement.addContent(rowElement);
        }
    }

    @Override
    public double[][] readValueFromXML(Element propElement) throws Exception {
        var rowChildren = propElement.getChildren();
        if (rowChildren.size() == 0) {
            return null;
        }
        var result = new double[rowChildren.size()][((Element) rowChildren.get(0)).getChildren().size()];
        var i = 0;
        var j = 0;
        for (var oe : rowChildren) {
            var re = (Element) oe;
            if (re.getName().equals(XML_ELEMENT_ROW)) {
                j = 0;
                for (var oc : re.getChildren()) {
                    result[i][j++] = Double.parseDouble(((Element) oc).getText());
                }
                i++;
            }
        }
        return result;
    }
}
