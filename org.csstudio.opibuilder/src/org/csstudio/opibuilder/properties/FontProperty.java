/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.properties.support.OPIFontPropertyDescriptor;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIFont;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

public class FontProperty extends AbstractWidgetProperty {

    /**
     * XML attribute name <code>font</code>.
     */
    public static final String XML_ELEMENT_FONT = "fontdata";

    /**
     * XML attribute name <code>fontName</code>.
     */
    public static final String XML_ELEMENT_FONTNAME = "opifont.name";

    /**
     * XML attribute name <code>fontName</code>.
     */
    public static final String XML_ATTRIBUTE_FONT_NAME = "fontName";

    /**
     * XML attribute name <code>fontName</code>.
     */
    public static final String XML_ATTRIBUTE_FONT_HEIGHT = "height";

    /**
     * XML attribute name <code>fontName</code>.
     */
    public static final String XML_ATTRIBUTE_FONT_STYLE = "style";

    public static final String XML_ATTRIBUTE_FONT_PIXELS = "pixels";

    private static final String QUOTE = "\"";

    /**
     * Font Property Constructor. The property value type is {@link OPIFont}.
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
    public FontProperty(String prop_id, String description, WidgetPropertyCategory category, FontData defaultValue) {
        super(prop_id, description, category, new OPIFont(defaultValue));
    }

    /**
     * Font Property Constructor. The property value type is {@link OPIFont}.
     * 
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. It must be a exist font macro name in font file.
     */
    public FontProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue) {
        super(prop_id, description, category, MediaService.getInstance().getOPIFont(defaultValue));
    }

    @Override
    public Object checkValue(Object value) {
        if (value == null) {
            return null;
        }

        var acceptedValue = value;

        if (value instanceof OPIFont) {
            // Avoid getFontData() as this method can be called from off the UI thread.
            if (((OPIFont) value).getRawFontData() == null) {
                acceptedValue = null;
            }
        } else if (value instanceof FontData) {
            acceptedValue = new OPIFont((FontData) value);
        } else if (value instanceof String) {
            acceptedValue = MediaService.getInstance().getOPIFont((String) value);
        } else {
            acceptedValue = null;
        }

        return acceptedValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new OPIFontPropertyDescriptor(prop_id, description);
    }

    @Override
    public void writeToXML(Element propElement) {
        var opiFont = (OPIFont) getPropertyValue();

        Element fontElement;

        if (opiFont.isPreDefined()) {
            fontElement = new Element(XML_ELEMENT_FONTNAME);
            fontElement.setText(opiFont.getFontMacroName());
        } else {
            fontElement = new Element(XML_ELEMENT_FONT);
        }
        var fontData = opiFont.getRawFontData();
        fontElement.setAttribute(XML_ATTRIBUTE_FONT_NAME, fontData.getName());
        fontElement.setAttribute(XML_ATTRIBUTE_FONT_HEIGHT, "" + fontData.getHeight());
        fontElement.setAttribute(XML_ATTRIBUTE_FONT_STYLE, "" + fontData.getStyle());
        fontElement.setAttribute(XML_ATTRIBUTE_FONT_PIXELS, "" + opiFont.isSizeInPixels());

        propElement.addContent(fontElement);
    }

    @Override
    public Object readValueFromXML(Element propElement) {
        var fontElement = propElement.getChild(XML_ELEMENT_FONT);
        if (fontElement != null) {
            // Create the OPIFont with the raw font data from the XML.
            var font = new OPIFont(new FontData(fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_NAME),
                    (int) Double.parseDouble(fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_HEIGHT)),
                    Integer.parseInt(fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_STYLE))));
            font.setSizeInPixels(Boolean.parseBoolean(fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_PIXELS)));
            return font;
        } else {
            fontElement = propElement.getChild(XML_ELEMENT_FONTNAME);
            if (fontElement != null) {
                OPIFont font = null;
                var fontName = fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_NAME);
                var fontHeight = fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_HEIGHT);
                var fontStyle = fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_STYLE);
                var heightInPixels = fontElement.getAttributeValue(XML_ATTRIBUTE_FONT_PIXELS);
                if (fontName != null && fontHeight != null && fontStyle != null) {
                    var fd = new FontData(fontName, (int) Double.parseDouble(fontHeight), Integer.parseInt(fontStyle));
                    font = MediaService.getInstance().getOPIFont(fontElement.getText(), fd);
                } else {
                    font = MediaService.getInstance().getOPIFont(fontElement.getText());
                }
                if (!font.isPreDefined()) {
                    // If this was serialised without a height in pixels attribute, it was from
                    // an older verison of BOY where points were assumed. To ensure the screens are not
                    // changed when re-saved, make this explicit.
                    if (heightInPixels != null) {
                        var inPixels = Boolean.parseBoolean(heightInPixels);
                        font.setSizeInPixels(inPixels);
                    } else {
                        font.setSizeInPixels(false);
                    }
                }
                return font;
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }

    @Override
    public String toStringInRuleScript(Object propValue) {
        var opiFont = (OPIFont) propValue;
        if (opiFont.isPreDefined()) {
            return QUOTE + opiFont.getFontMacroName() + QUOTE;
        } else {
            var fontData = opiFont.getFontData();
            return "ColorFontUtil.getFont(\"" + fontData.getName() + QUOTE + "," + fontData.getHeight() + ","
                    + fontData.getStyle() + ")";
        }
    }

}
