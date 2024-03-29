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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

/**
 * The base widget property class for all kinds of widget property.
 */
public abstract class AbstractWidgetProperty<T> {

    protected String prop_id;

    protected String description;

    private PropertyChangeSupport pcsDelegate;

    private PropertyDescriptor propertyDescriptor;

    protected T propertyValue;

    protected T defaultValue;

    protected WidgetPropertyCategory category;

    protected boolean visibleInPropSheet;

    /**
     * If this property will be saved to opi xml file.
     */
    private boolean isSavable = true;

    protected ExecutionMode executionMode = ExecutionMode.EDIT_MODE;

    protected AbstractWidgetModel widgetModel;

    /**
     * Widget Property Constructor
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. It cannot be null.
     */
    public AbstractWidgetProperty(String prop_id, String description, WidgetPropertyCategory category,
            T defaultValue) {
        this.prop_id = prop_id;
        this.description = description;
        this.category = category;
        this.visibleInPropSheet = true;
        this.defaultValue = defaultValue;
        this.propertyValue = defaultValue;
        pcsDelegate = new PropertyChangeSupport(this);
    }

    /**
     * Add listener on property change event. The listener will be removed when widget deactivated, so it is better to
     * call this method in edit part during activating the widget to make sure the widget always have necessary
     * listeners added.
     *
     * @param listener
     *            the listener which will be notified when property value changed.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        pcsDelegate.addPropertyChangeListener(listener);
    }

    /**
     * Check if the requestNewValue is convertible or legal.
     *
     * @param value
     *            the value to be checked.
     * @return The value after being checked. It might be coerced if the requestValue is illegal. return null if it is
     *         not convertible or illegal.
     */
    public abstract T checkValue(Object value);

    public void firePropertyChange(T oldValue, T newValue) {
        if (pcsDelegate.hasListeners(prop_id)) {
            pcsDelegate.firePropertyChange(prop_id, oldValue, newValue);
        }
    }

    public final WidgetPropertyCategory getCategory() {
        return category;
    }

    public final T getDefaultValue() {
        return defaultValue;
    }

    public boolean isDefaultValue() {
        return defaultValue.equals(propertyValue);
    }

    public final String getDescription() {
        return description;
    }

    public final IPropertyDescriptor getPropertyDescriptor() {
        if (propertyDescriptor == null) {
            createPropertyDescriptor(visibleInPropSheet);
        }
        return propertyDescriptor;
    }

    public final String getPropertyID() {
        return prop_id;
    }

    public T getPropertyValue() {
        return propertyValue;
    }

    /**
     * @return the raw property value that should not be treated or replaced by macros.
     */
    public T getRawPropertyValue() {
        return propertyValue;
    }

    /**
     * Get the formatted value to be displayed in property sheet.
     */
    // public abstract Object getFormattedPropertyValue();

    public final boolean isVisibleInPropSheet() {
        return visibleInPropSheet;
    }

    public void removeAllPropertyChangeListeners() {
        for (var l : pcsDelegate.getPropertyChangeListeners()) {
            // if(l instanceof WidgetPropertyChangeListener)
            // ((WidgetPropertyChangeListener) l).removeAllHandlers();
            pcsDelegate.removePropertyChangeListener(l);
        }
    }

    public final PropertyChangeListener[] getAllPropertyChangeListeners() {
        return pcsDelegate.getPropertyChangeListeners();
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener instanceof WidgetPropertyChangeListener) {
            ((WidgetPropertyChangeListener) listener).removeAllHandlers();
        }
        pcsDelegate.removePropertyChangeListener(listener);
    }

    /**
     * If the property should be saved to xml file.
     */
    public boolean isSavable() {
        return isSavable;
    }

    /**
     * Set to true if the property should be saved to xml file.
     */
    public void setSavable(boolean isSavable) {
        this.isSavable = isSavable;
    }

    public void setCategory(WidgetPropertyCategory category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
        createPropertyDescriptor(visibleInPropSheet);
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Set property value and fire the listeners on the property.
     */
    public void setPropertyValue(Object value) {
        // do conversion and legally check
        var newValue = checkValue(value);
        if (newValue == null || newValue.equals(propertyValue)) {
            return;
        }
        var oldValue = getPropertyValue();
        propertyValue = newValue;
        firePropertyChange(oldValue, getPropertyValue());
    }

    /**
     * Set property value and fire the listeners on the property.oldValue will be set as null.
     */
    public void setPropertyValue_IgnoreOldValue(Object value) {
        // do conversion and legally check
        var newValue = checkValue(value);
        if (newValue == null || newValue.equals(propertyValue)) {
            return;
        }
        propertyValue = newValue;
        firePropertyChange(null, getPropertyValue());
    }

    /**
     * Set the property value.
     *
     * @param value
     *            the value to be set.
     * @param fire
     *            true if listeners should be fired regardless the old value. If false, only set the property value
     *            without firing listeners.
     */
    public void setPropertyValue(Object value, boolean fire) {
        if (fire) {
            // do conversion and legally check
            var newValue = checkValue(value);
            if (newValue == null) {
                return;
            }
            propertyValue = newValue;
            firePropertyChange(null, getPropertyValue());
        } else {
            var newValue = checkValue(value);
            if (newValue == null || newValue.equals(propertyValue)) {
                return;
            }
            propertyValue = newValue;
        }
    }

    /**
     * @param visibleInPropSheet
     * @return true if visibility changed.
     */
    public final boolean setVisibleInPropSheet(boolean visibleInPropSheet) {
        if (visibleInPropSheet == this.visibleInPropSheet) {
            return false;
        }
        createPropertyDescriptor(visibleInPropSheet);
        this.visibleInPropSheet = visibleInPropSheet;
        return true;
    }

    private void createPropertyDescriptor(boolean visibleInPropSheet) {
        if (visibleInPropSheet) {
            propertyDescriptor = createPropertyDescriptor();
            if (propertyDescriptor != null) {
                propertyDescriptor.setCategory(category == null ? null : category.toString());
            }
        } else {
            propertyDescriptor = null;
        }
    }

    /**
     * Create the {@link IPropertyDescriptor}
     */
    protected abstract PropertyDescriptor createPropertyDescriptor();

    /**
     * Write the property value into a XML element.
     */
    public abstract void writeToXML(Element propElement);

    /**
     * Read the property value from a XML element.
     */
    public abstract T readValueFromXML(Element propElement) throws Exception;

    public void setWidgetModel(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
        if (widgetModel != null) {
            setExecutionMode(widgetModel.getExecutionMode());
        }
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    /**
     * Subclass should override this method if it is configurable by rule. If this returns true, the method
     * {@link #toStringInRuleScript(Object)} should be properly overridden too.
     *
     * @return true if this property is configurable by rule.
     */
    public boolean configurableByRule() {
        return false;
    }

    /**
     * Subclass should override this method if it only accept output expression.
     *
     * @return true if this property only accept output expression.
     */
    public boolean onlyAcceptExpressionInRule() {
        return false;
    }

    /**
     * Convert to the property value string in the script generated by rule.
     */
    public String toStringInRuleScript(T propValue) {
        if (propValue != null) {
            return propValue.toString();
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return widgetModel.getName() + "." + prop_id + ": " + getPropertyValue();
    }
}
