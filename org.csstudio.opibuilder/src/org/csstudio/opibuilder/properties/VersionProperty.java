/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.jdom.Element;

/**
 * Version property.
 */
public class VersionProperty extends UnchangableStringProperty {

    public VersionProperty(String prop_id, String description,
            WidgetPropertyCategory category, String defaultValue) {
        super(prop_id, description, category, defaultValue);
    }

    @Override
    public void writeToXML(Element propElement) {
        setPropertyValue(OPIBuilderPlugin.getDefault().getBundle().getVersion().toString());
        super.writeToXML(propElement);
    }

    @Override
    public boolean configurableByRule() {
        return false;
    }

}
