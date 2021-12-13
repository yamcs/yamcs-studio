/********************************************************************************
 * Copyright (c) 2012, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.datadefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.eclipse.core.runtime.Assert;

/**
 * The abstract data that holds multiple properties.
 */
public abstract class AbstractComplexData {
    private Map<String, AbstractWidgetProperty> propertyMap;

    private AbstractWidgetModel widgetModel;

    public AbstractComplexData(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
        propertyMap = new LinkedHashMap<>();
        configureProperties();
    }

    /**
     * Add a property to the widget.
     * 
     * @param property
     *            the property to be added.
     */
    public void addProperty(AbstractWidgetProperty property) {
        Assert.isNotNull(property);
        property.setWidgetModel(getWidgetModel());
        propertyMap.put(property.getPropertyID(), property);
    }

    /**
     * The place to add properties.
     */
    protected abstract void configureProperties();

    public AbstractWidgetProperty[] getAllProperties() {
        var propArray = new AbstractWidgetProperty[propertyMap.size()];
        var i = 0;
        for (var p : propertyMap.values()) {
            propArray[i++] = p;
        }
        return propArray;
    }

    public Set<String> getAllPropertyIDs() {
        return propertyMap.keySet();
    }

    public AbstractComplexData getCopy() {
        var copy = createInstance();
        for (var id : propertyMap.keySet()) {
            copy.setPropertyValue(id, getPropertyValue(id));
        }
        copy.setWidgetModel(getWidgetModel());
        return copy;
    }

    public AbstractWidgetProperty getProperty(String propId) {
        return propertyMap.get(propId);
    }

    public Object getPropertyValue(Object id) {
        Assert.isTrue(propertyMap.containsKey(id));
        return propertyMap.get(id).getPropertyValue();
    }

    /**
     * @return the widgetModel
     */
    public AbstractWidgetModel getWidgetModel() {
        return widgetModel;
    }

    public void setPropertyValue(Object id, Object value) {
        Assert.isTrue(propertyMap.containsKey(id));
        propertyMap.get(id).setPropertyValue(value);
    }

    /**
     * @param widgetModel
     *            the widgetModel to set
     */
    public void setWidgetModel(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
        for (var property : getAllProperties()) {
            property.setWidgetModel(widgetModel);
        }
    }

    /**
     * @return a new instance of this data.
     */
    public abstract AbstractComplexData createInstance();

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AbstractComplexData)) {
            return false;
        }
        var objData = ((AbstractComplexData) obj);
        for (var property : getAllProperties()) {
            if (objData.getProperty(property.getPropertyID()) == null) {
                return false;
            }
            if (!(property.getPropertyValue().equals(objData.getPropertyValue(property.getPropertyID())))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        var properties = getAllProperties();
        var result = getClass().hashCode();
        for (var p : properties) {
            result = 31 * result + p.getPropertyID().hashCode();
            result = 31 * result + (p.getPropertyValue() == null ? 0 : p.getPropertyValue().hashCode());
        }
        return result;
    }
}
