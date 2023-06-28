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

import org.csstudio.opibuilder.properties.support.CommandPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * The widget property for a command name. It also accepts macro string $(macro).
 */
public class CommandProperty extends StringProperty {

    private String detailDescription;

    /**
     * Command Property Constructor. The property value type is {@link String}.
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
    public CommandProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue) {
        super(prop_id, description, category, defaultValue, false, false);
        setDetailedDescription(description);
    }

    /**
     * Set detailed description to be displayed on tooltip and status line
     */
    public void setDetailedDescription(String detailDescription) {
        this.detailDescription = detailDescription;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new CommandPropertyDescriptor(prop_id, description, detailDescription);
    }
}
