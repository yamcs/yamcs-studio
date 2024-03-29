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

import org.csstudio.opibuilder.properties.support.OPIColorPropertyDescriptor;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIColor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

/**
 * The widget property for color.
 */
public class ColorProperty extends AbstractWidgetProperty<OPIColor> {

    /**
     * XML attribute name <code>color</code>.
     */
    public static final String XML_ELEMENT_COLOR = "color";

    /**
     * XML attribute name <code>color</code>.
     */
    public static final String XML_ATTRIBUTE_NAME = "name";

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

    private static final String QUOTE = "\"";

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
     */
    public ColorProperty(String prop_id, String description, WidgetPropertyCategory category, RGB defaultValue) {
        super(prop_id, description, category, new OPIColor(defaultValue));
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
     *            the default value when the widget is first created. It must be a color macro name in color file.
     */
    public ColorProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue) {
        super(prop_id, description, category, MediaService.getInstance().getOPIColor(defaultValue));
    }

    @Override
    public OPIColor checkValue(Object value) {
        if (value == null) {
            return null;
        }

        OPIColor acceptedValue;
        if (value instanceof OPIColor) {
            if (((OPIColor) value).getRGBValue() == null) {
                acceptedValue = null;
            } else {
                acceptedValue = (OPIColor) value;
            }
        } else if (value instanceof RGB) {
            acceptedValue = new OPIColor((RGB) value);
        } else if (value instanceof String) {
            acceptedValue = MediaService.getInstance().getOPIColor((String) value);
        } else {
            acceptedValue = null;
        }

        return acceptedValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new OPIColorPropertyDescriptor(prop_id, description);
    }

    @Override
    public void writeToXML(Element propElement) {
        var opiColor = getPropertyValue();
        Element colorElement;
        colorElement = new Element(XML_ELEMENT_COLOR);
        if (opiColor.isPreDefined()) {
            colorElement.setAttribute(XML_ATTRIBUTE_NAME, opiColor.getColorName());
        }
        var color = opiColor.getRGBValue();
        colorElement.setAttribute(XML_ATTRIBUTE_RED, "" + color.red);
        colorElement.setAttribute(XML_ATTRIBUTE_GREEN, "" + color.green);
        colorElement.setAttribute(XML_ATTRIBUTE_BLUE, "" + color.blue);
        propElement.addContent(colorElement);
    }

    @Override
    public OPIColor readValueFromXML(Element propElement) {
        var colorElement = propElement.getChild(XML_ELEMENT_COLOR);
        var name = colorElement.getAttributeValue(XML_ATTRIBUTE_NAME);
        if (name == null) {
            var result = new RGB(Integer.parseInt(colorElement.getAttributeValue(XML_ATTRIBUTE_RED)),
                    Integer.parseInt(colorElement.getAttributeValue(XML_ATTRIBUTE_GREEN)),
                    Integer.parseInt(colorElement.getAttributeValue(XML_ATTRIBUTE_BLUE)));
            return new OPIColor(result);
        } else {
            var red = colorElement.getAttributeValue(XML_ATTRIBUTE_RED);
            var green = colorElement.getAttributeValue(XML_ATTRIBUTE_GREEN);
            var blue = colorElement.getAttributeValue(XML_ATTRIBUTE_BLUE);
            RGB rgb;
            if (red != null && green != null && blue != null) {
                rgb = new RGB(Integer.parseInt(red), Integer.parseInt(green), Integer.parseInt(blue));
                return MediaService.getInstance().getOPIColor(name, rgb);
            }
            return MediaService.getInstance().getOPIColor(name);
        }
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }

    @Override
    public String toStringInRuleScript(OPIColor opiColor) {
        if (opiColor.isPreDefined()) {
            if (MediaService.getInstance().isColorNameDefined(opiColor.getColorName())) {
                return QUOTE + opiColor.getColorName() + QUOTE;
            }
        }

        var rgb = opiColor.getRGBValue();
        return "ColorFontUtil.getColorFromRGB(" + rgb.red + "," + rgb.green + "," + rgb.blue + ")";
    }
}
