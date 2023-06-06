/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.properties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.StringMapPropertyDescriptor;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

public class StringMapProperty extends AbstractWidgetProperty<Map<String, String>> {

    /**
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. It can be null.
     */
    public StringMapProperty(String prop_id, String description, WidgetPropertyCategory category,
            Map<String, String> defaultValue) {
        super(prop_id, description, category, defaultValue == null ? Collections.emptyMap() : defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> checkValue(Object value) {
        if (value == null) {
            return null;
        }
        Map<String, String> acceptableValue = null;
        if (value instanceof Map) {
            var mapValue = (Map<?, ?>) value;
            if (mapValue.isEmpty()
                    || (mapValue.size() > 0
                            && mapValue.entrySet().iterator().next().getKey() instanceof String
                            && mapValue.entrySet().iterator().next().getValue() instanceof String)) {
                acceptableValue = (Map<String, String>) value;
            }
        }
        return acceptableValue;
    }

    @Override
    public Map<String, String> getPropertyValue() {
        if (executionMode == ExecutionMode.RUN_MODE && widgetModel != null) {
            var result = new LinkedHashMap<String, String>();
            for (var item : super.getPropertyValue().entrySet()) {
                result.put(item.getKey(),
                        OPIBuilderMacroUtil.replaceMacros(widgetModel, item.getValue()));
            }
            return result;
        } else {
            return super.getPropertyValue();
        }
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new StringMapPropertyDescriptor(prop_id, description);
    }

    @Override
    public Map<String, String> readValueFromXML(Element propElement) {
        var result = new LinkedHashMap<String, String>();
        for (var oe : propElement.getChildren()) {
            var se = (Element) oe;
            result.put(se.getName(), se.getText());
        }
        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        for (var entry : propertyValue.entrySet()) {
            var newElement = new Element(entry.getKey());
            newElement.setText(entry.getValue());
            propElement.addContent(newElement);
        }
    }
}
