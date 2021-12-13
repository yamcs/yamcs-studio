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
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.support.ActionsPropertyDescriptor;
import org.csstudio.opibuilder.widgetActions.ActionsInput;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**
 * The widget property for actions.
 */
public class ActionsProperty extends AbstractWidgetProperty<ActionsInput> {

    /**
     * XML ELEMENT name <code>ACTION</code>.
     */
    public static final String XML_ELEMENT_ACTION = "action";

    /**
     * XML ATTRIBUTE name <code>PATHSTRING</code>.
     */
    public static final String XML_ATTRIBUTE_ACTION_TYPE = "type";

    /**
     * XML ATTRIBUTE name <code>HOOK</code>.
     */
    public static final String XML_ATTRIBUTE_HOOK_FIRST = "hook";

    public static final String XML_ATTRIBUTE_HOOK_ALL = "hook_all";

    private boolean showHookOption;

    /**
     * Widget Property Constructor
     * 
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     */
    public ActionsProperty(String prop_id, String description, WidgetPropertyCategory category) {
        super(prop_id, description, category, new ActionsInput());
        showHookOption = true;
    }

    /**
     * Widget Property Constructor
     * 
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param showHookOption
     *            true if the hook option is visible in the dialog.
     */
    public ActionsProperty(String prop_id, String description, WidgetPropertyCategory category,
            boolean showHookOption) {
        super(prop_id, description, category, new ActionsInput());
        this.showHookOption = showHookOption;
    }

    @Override
    public ActionsInput checkValue(Object value) {
        if (value == null) {
            return null;
        }
        ActionsInput acceptableValue = null;
        if (value instanceof ActionsInput) {
            ((ActionsInput) value).setWidgetModel(widgetModel);
            acceptableValue = (ActionsInput) value;
        }

        return acceptableValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new ActionsPropertyDescriptor(prop_id, description, showHookOption);
    }

    @Override
    public ActionsInput readValueFromXML(Element propElement) {
        var result = new ActionsInput();
        result.setHookUpFirstActionToWidget(
                Boolean.parseBoolean(propElement.getAttributeValue(XML_ATTRIBUTE_HOOK_FIRST)));
        if (propElement.getAttribute(XML_ATTRIBUTE_HOOK_ALL) != null) {
            result.setHookUpAllActionsToWidget(
                    Boolean.parseBoolean(propElement.getAttributeValue(XML_ATTRIBUTE_HOOK_ALL)));
        }
        for (var oe : propElement.getChildren(XML_ELEMENT_ACTION)) {
            var se = (Element) oe;
            var action = WidgetActionFactory
                    .createWidgetAction(ActionType.parseAction(se.getAttributeValue(XML_ATTRIBUTE_ACTION_TYPE)));
            if (action != null) {
                var children = se.getChildren();
                var iterator = children.iterator();
                var propIdSet = action.getAllPropertyIDs();
                while (iterator.hasNext()) {
                    var subElement = (Element) iterator.next();
                    // handle property
                    if (propIdSet.contains(subElement.getName())) {
                        var propId = subElement.getName();
                        try {
                            action.setPropertyValue(propId, action.getProperty(propId).readValueFromXML(subElement));
                        } catch (Exception e) {
                            var errorMessage = "Failed to read the " + propId + " property for "
                                    + action.getDescription() + ". "
                                    + "The default property value will be set instead. \n" + e;
                            OPIBuilderPlugin.getLogger().log(Level.WARNING, errorMessage, e);
                        }
                    }
                }
                result.getActionsList().add(action);
            }
        }

        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        var actionsInput = getPropertyValue();
        propElement.setAttribute(XML_ATTRIBUTE_HOOK_FIRST, "" + actionsInput.isFirstActionHookedUpToWidget()); //
        propElement.setAttribute(XML_ATTRIBUTE_HOOK_ALL, "" + actionsInput.isHookUpAllActionsToWidget()); //

        for (var action : actionsInput.getActionsList()) {
            var actionElement = new Element(XML_ELEMENT_ACTION);
            actionElement.setAttribute(XML_ATTRIBUTE_ACTION_TYPE, action.getActionType().toString());
            for (var property : action.getAllProperties()) {
                var propEle = new Element(property.getPropertyID());
                property.writeToXML(propEle);
                actionElement.addContent(propEle);
            }
            propElement.addContent(actionElement);
        }
    }

    @Override
    public void setWidgetModel(AbstractWidgetModel widgetModel) {
        super.setWidgetModel(widgetModel);
        getPropertyValue().setWidgetModel(widgetModel);
    }
}
