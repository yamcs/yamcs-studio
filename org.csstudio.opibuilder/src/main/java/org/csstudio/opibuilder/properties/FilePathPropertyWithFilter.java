/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.properties.support.FilePathPropertyDescriptorWithFilter;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * A custom file path property applying filters on image resource name.
 */
public class FilePathPropertyWithFilter extends FilePathProperty {

    /**
     * The resource names which should be accepted.
     */
    private String[] filters;

    public FilePathPropertyWithFilter(String propertyID, String description, WidgetPropertyCategory category,
            String defaultValue, String[] filters) {
        super(propertyID, description, category, defaultValue, filters);
        this.filters = filters;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new FilePathPropertyDescriptorWithFilter(prop_id, description, widgetModel, filters);
    }
}
