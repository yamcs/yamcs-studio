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

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.datadefinition.AbstractComplexData;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.support.ComplexDataPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**
 * A property that can hold data with multiple properties.
 */
public class ComplexDataProperty extends AbstractWidgetProperty<AbstractComplexData> {

    public static final String XML_ATTRIBUTE_TYPE = "type";

    private String dialogTitle;

    /**
     * Widget Property Constructor
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultData
     *            default value. It cannot be null.
     * @param dialogTitle
     *            title of the dialog for editing the complex data.
     */
    public ComplexDataProperty(String prop_id, String description, WidgetPropertyCategory category,
            AbstractComplexData defaultData, String dialogTitle) {
        super(prop_id, description, category, defaultData);
        this.dialogTitle = dialogTitle;
    }

    @Override
    public AbstractComplexData checkValue(Object value) {
        if (value == null) {
            return null;
        }
        AbstractComplexData acceptableValue = null;
        if (value instanceof AbstractComplexData) {
            ((AbstractComplexData) value).setWidgetModel(widgetModel);
            acceptableValue = (AbstractComplexData) value;
        }

        return acceptableValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new ComplexDataPropertyDescriptor(prop_id, description, dialogTitle);
    }

    @Override
    public AbstractComplexData readValueFromXML(Element propElement) {
        var result = getDefaultValue().createInstance();

        var children = propElement.getChildren();
        var iterator = children.iterator();
        var propIdSet = result.getAllPropertyIDs();
        while (iterator.hasNext()) {
            var subElement = (Element) iterator.next();
            // handle property
            if (propIdSet.contains(subElement.getName())) {
                var propId = subElement.getName();
                try {
                    result.setPropertyValue(propId, result.getProperty(propId).readValueFromXML(subElement));
                } catch (Exception e) {
                    var errorMessage = "Failed to read the " + propId + " sub property for " + getPropertyID() + ". "
                            + "The default property value will be set instead. \n" + e;
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, errorMessage, e);
                }
            }
        }

        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        for (var property : getPropertyValue().getAllProperties()) {
            var propEle = new Element(property.getPropertyID());
            property.writeToXML(propEle);
            propElement.addContent(propEle);
        }
    }

    @Override
    public void setWidgetModel(AbstractWidgetModel widgetModel) {
        super.setWidgetModel(widgetModel);
        getPropertyValue().setWidgetModel(widgetModel);
    }
}
