/*******************************************************************************
 * Copyright (c) 2022 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.properties.support.JavaScriptPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * The widget property for string. It also accept macro string $(macro).
 */
public class EmbeddedJavaScriptProperty extends StringProperty {

    public EmbeddedJavaScriptProperty(String prop_id, String description, WidgetPropertyCategory category,
            String defaultValue) {
        super(prop_id, description, category, defaultValue, true, true);
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new JavaScriptPropertyDescriptor(prop_id, description);
    }

    @Override
    public boolean configurableByRule() {
        return false;
    }
}
