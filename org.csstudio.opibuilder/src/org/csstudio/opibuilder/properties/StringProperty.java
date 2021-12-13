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

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.MultiLineTextPropertyDescriptor;
import org.csstudio.opibuilder.script.RuleData;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.CDATA;
import org.jdom.Element;

/**
 * The widget property for string. It also accept macro string $(macro).
 */
public class StringProperty extends AbstractWidgetProperty {

    private boolean multiLine, saveAsCDATA;

    /**
     * String Property Constructor. The property value type is {@link String}.
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
    public StringProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue) {
        this(prop_id, description, category, defaultValue, false, false);
    }

    public StringProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue,
            boolean multiLine) {
        this(prop_id, description, category, defaultValue, multiLine, false);
    }

    public StringProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue,
            boolean multiLine, boolean saveAsCDATA) {
        super(prop_id, description, category, defaultValue);
        this.multiLine = multiLine;
        this.saveAsCDATA = saveAsCDATA;
    }

    @Override
    public Object checkValue(Object value) {
        if (value == null) {
            return null;
        }

        String acceptedValue = null;

        if (value instanceof String) {
            acceptedValue = (String) value;
        } else {
            acceptedValue = value.toString();
        }

        return acceptedValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        if (multiLine) {
            return new MultiLineTextPropertyDescriptor(prop_id, description);
        } else {
            return new TextPropertyDescriptor(prop_id, description);
        }
    }

    @Override
    public void writeToXML(Element propElement) {
        if (saveAsCDATA) {
            propElement.setContent(new CDATA(getPropertyValue().toString()));
        } else {
            var reShapedString = getPropertyValue().toString().replaceAll("\\x0D\\x0A?",
                    new String(new byte[] { 13, 10 }));
            propElement.setText(reShapedString);
        }
    }

    @Override
    public Object readValueFromXML(Element propElement) {
        return propElement.getValue();
    }

    @Override
    public Object getPropertyValue() {
        if (widgetModel != null && widgetModel.getExecutionMode() == ExecutionMode.RUN_MODE) {
            return OPIBuilderMacroUtil.replaceMacros(widgetModel, (String) super.getPropertyValue());
        } else {
            return super.getPropertyValue();
        }
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }

    @Override
    public String toStringInRuleScript(Object propValue) {
        return RuleData.QUOTE + super.toStringInRuleScript(propValue) + RuleData.QUOTE;
    }
}
