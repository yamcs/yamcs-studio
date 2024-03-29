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

import java.util.LinkedHashMap;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.MacrosPropertyDescriptor;
import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

/**
 * The property for macros.
 */
public class MacrosProperty extends AbstractWidgetProperty<MacrosInput> {

    /**
     * XML ELEMENT name <code>INCLUDE_PARENT_MACROS</code>.
     */
    public static final String XML_ELEMENT_INCLUDE_PARENT_MACROS = "include_parent_macros";

    /**
     * Macros Property Constructor. The property value type is {@link MacrosInput}.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param default_macros
     *            the default macros when the widget is first created.
     */
    public MacrosProperty(String prop_id, String description, WidgetPropertyCategory category,
            MacrosInput default_macros) {
        super(prop_id, description, category, default_macros);
    }

    @Override
    public MacrosInput checkValue(Object value) {
        if (value == null) {
            return null;
        }
        MacrosInput acceptableValue = null;
        if (value instanceof MacrosInput) {
            acceptableValue = (MacrosInput) value;
        }

        return acceptableValue;
    }

    @Override
    public MacrosInput getPropertyValue() {
        if (executionMode == ExecutionMode.RUN_MODE && widgetModel != null) {
            var value = super.getPropertyValue().getCopy();
            for (var key : value.getMacrosMap().keySet()) {
                var newS = OPIBuilderMacroUtil.replaceMacros(widgetModel, value.getMacrosMap().get(key));
                if (!newS.equals(value.getMacrosMap().get(key))) {
                    value.getMacrosMap().put(key, newS);
                }
            }
            return value;
        } else {
            return super.getPropertyValue();
        }
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new MacrosPropertyDescriptor(prop_id, description);
    }

    @Override
    public MacrosInput readValueFromXML(Element propElement) {
        var macros = new LinkedHashMap<String, String>();
        var b = true;
        for (var oe : propElement.getChildren()) {
            var se = (Element) oe;
            if (se.getName().equals(XML_ELEMENT_INCLUDE_PARENT_MACROS)) {
                b = Boolean.parseBoolean(se.getText());
            } else {
                macros.put(se.getName(), se.getText());
            }
        }
        return new MacrosInput(macros, b);
    }

    @Override
    public void writeToXML(Element propElement) {
        var macros = propertyValue;
        var be = new Element(XML_ELEMENT_INCLUDE_PARENT_MACROS);
        be.setText("" + macros.isInclude_parent_macros());
        propElement.addContent(be);
        for (var key : macros.getMacrosMap().keySet()) {
            var newElement = new Element(key);
            newElement.setText(macros.getMacrosMap().get(key));
            propElement.addContent(newElement);
        }
    }
}
