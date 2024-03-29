/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.properties;

import java.util.LinkedHashMap;

import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.widgets.model.IntensityGraphModel;
import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

public class ColorMapProperty extends AbstractWidgetProperty<ColorMap> {

    /**
     * XML ELEMENT name <code>PREDEFINEDCOLOR</code>.
     */
    public static final String XML_ELEMENT_MAP = "map";
    /**
     * XML ELEMENT name <code>MAP</code>.
     */
    public static final String XML_ELEMENT_E = "e";
    /**
     * XML Element name <code>INTERPOLATE</code>.
     */
    public static final String XML_ELEMENT_INTERPOLATE = "interpolate";
    /**
     * XML Element name <code>AUTOSCALE</code>.
     */
    public static final String XML_ELEMENT_AUTOSCALE = "autoscale";

    /**
     * XML attribute name <code>red</code>.
     */
    public static final String XML_ATTRIBUTE_RED = "red";

    /**
     * XML attribute name <code>green</code>.
     */
    public static final String XML_ATTRIBUTE_GREEN = "green";

    /**
     * XML attribute name <code>blue</code>.
     */
    public static final String XML_ATTRIBUTE_BLUE = "blue";

    public ColorMapProperty(String prop_id, String description, WidgetPropertyCategory category,
            ColorMap defaultValue) {
        super(prop_id, description, category, defaultValue);
    }

    @Override
    public ColorMap checkValue(Object value) {
        if (value == null) {
            return null;
        }
        ColorMap acceptableValue = null;
        if (value instanceof ColorMap) {
            if (((ColorMap) value).getMap().size() >= 2) {
                acceptableValue = (ColorMap) value;
            }
        } else if (value instanceof String) {
            for (var map : ColorMap.PredefinedColorMap.values()) {
                if (map.toString().equals(value)) {
                    acceptableValue = new ColorMap(map, true, true);
                    break;
                }
            }
        }

        return acceptableValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new ColorMapPropertyDescriptor(prop_id, description, (IntensityGraphModel) widgetModel);
    }

    @Override
    public ColorMap readValueFromXML(Element propElement) {
        var result = new ColorMap();
        result.setInterpolate(Boolean.parseBoolean(propElement.getChild(XML_ELEMENT_INTERPOLATE).getValue()));
        result.setAutoScale(Boolean.parseBoolean(propElement.getChild(XML_ELEMENT_AUTOSCALE).getValue()));
        if (propElement.getChild(XML_ELEMENT_MAP).getChildren().size() == 0) {
            var p = PredefinedColorMap.fromIndex(Integer.parseInt(propElement.getChild(XML_ELEMENT_MAP).getValue()));
            result.setPredefinedColorMap(p);
        } else {
            var map = new LinkedHashMap<Double, RGB>();
            for (var o : propElement.getChild(XML_ELEMENT_MAP).getChildren()) {
                var e = (Element) o;
                map.put(Double.parseDouble(e.getValue()),
                        new RGB(Integer.parseInt(e.getAttributeValue(XML_ATTRIBUTE_RED)),
                                Integer.parseInt(e.getAttributeValue(XML_ATTRIBUTE_GREEN)),
                                Integer.parseInt(e.getAttributeValue(XML_ATTRIBUTE_BLUE))));
            }
            result.setColorMap(map);
        }

        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        var colorMap = getPropertyValue();
        var interpolateElement = new Element(XML_ELEMENT_INTERPOLATE);
        interpolateElement.setText(Boolean.toString(colorMap.isInterpolate()));
        var autoScaleElement = new Element(XML_ELEMENT_AUTOSCALE);
        autoScaleElement.setText(Boolean.toString(colorMap.isAutoScale()));

        var preDefinedElement = new Element(XML_ELEMENT_MAP);
        if (colorMap.getPredefinedColorMap() == PredefinedColorMap.None) {
            for (var k : colorMap.getMap().keySet()) {
                var colorElement = new Element(XML_ELEMENT_E);
                colorElement.setText(k.toString());
                var color = colorMap.getMap().get(k);
                colorElement.setAttribute(XML_ATTRIBUTE_RED, "" + color.red);
                colorElement.setAttribute(XML_ATTRIBUTE_GREEN, "" + color.green);
                colorElement.setAttribute(XML_ATTRIBUTE_BLUE, "" + color.blue);
                preDefinedElement.addContent(colorElement);
            }
        } else {
            preDefinedElement.setText(Integer.toString(PredefinedColorMap.toIndex(colorMap.getPredefinedColorMap())));
        }
        propElement.addContent(interpolateElement);
        propElement.addContent(autoScaleElement);
        propElement.addContent(preDefinedElement);
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }

    @Override
    public boolean onlyAcceptExpressionInRule() {
        return true;
    }
}
