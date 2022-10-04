/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.commands;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.gef.commands.Command;

/**
 * A Command to set a property value of a widget. Use command can help to realize redo/undo.
 */
public class SetWidgetPropertyCommand extends Command {

    /**
     * The {@link AbstractWidgetModel}.
     */
    private AbstractWidgetModel widgetModel;
    /**
     * The name of the property.
     */
    private String prop_id;
    /**
     * The new value for the property.
     */
    private Object newValue;
    /**
     * The old value of the property.
     */
    private Object oldValue;

    /**
     * Constructor.
     *
     * @param widget
     *            The widget, whose property value should be set
     * @param prop_id
     *            The id of the property
     * @param newValue
     *            The new value for the property
     */
    public SetWidgetPropertyCommand(AbstractWidgetModel widget, String prop_id, Object newValue) {
        widgetModel = widget;
        this.prop_id = prop_id;
        this.newValue = newValue;
        setLabel("Set " + prop_id);
    }

    @Override
    public void execute() {
        oldValue = widgetModel.getPropertyValue(prop_id);
        widgetModel.setPropertyValue(prop_id, newValue);
    }

    @Override
    public void undo() {
        widgetModel.setPropertyValue(prop_id, oldValue);
    }
}
