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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.StringListPropertyDescriptor;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

/**
 * The property for string list.
 */
public class StringListProperty extends AbstractWidgetProperty<List<String>> {

    /**
     * XML ELEMENT name <code>INCLUDE_PARENT_MACROS</code>.
     */
    public static final String XML_ELEMENT_ITEM = "s";

    /**
     * StringList Property Constructor. The property value type is {@link List} of Strings.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. It can be null.
     */
    public StringListProperty(String prop_id, String description, WidgetPropertyCategory category,
            List<String> defaultValue) {
        super(prop_id, description, category, defaultValue == null ? Collections.emptyList() : defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> checkValue(Object value) {
        if (value == null) {
            return null;
        }
        List<String> acceptableValue = null;
        if (value instanceof List) {
            if (((List<?>) value).size() == 0
                    || (((List<?>) value).size() > 0 && ((List<?>) value).get(0) instanceof String)) {
                acceptableValue = (List<String>) value;
            }
        }
        return acceptableValue;
    }

    @Override
    public List<String> getPropertyValue() {
        if (widgetModel != null && widgetModel.getExecutionMode() == ExecutionMode.RUN_MODE) {
            var result = new ArrayList<String>();
            for (var item : super.getPropertyValue()) {
                result.add(OPIBuilderMacroUtil.replaceMacros(widgetModel, item));
            }
            return result;
        } else {
            return super.getPropertyValue();
        }
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new StringListPropertyDescriptor(prop_id, description);
    }

    @Override
    public List<String> readValueFromXML(Element propElement) {
        var result = new ArrayList<String>();
        for (var oe : propElement.getChildren()) {
            var se = (Element) oe;
            if (se.getName().equals(XML_ELEMENT_ITEM)) {
                result.add(se.getText());
            }
        }
        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        for (var item : propertyValue) {
            var newElement = new Element(XML_ELEMENT_ITEM);
            newElement.setText(item);
            propElement.addContent(newElement);
        }
    }
}
