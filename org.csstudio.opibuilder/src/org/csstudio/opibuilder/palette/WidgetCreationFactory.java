/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.palette;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.util.WidgetDescriptor;
import org.eclipse.gef.requests.CreationFactory;

/**
 * The CreationFactory to create the widget.
 */
public class WidgetCreationFactory implements CreationFactory {

    private final WidgetDescriptor widgetDescriptor;
    private AbstractWidgetModel widgetModel = null;

    public WidgetCreationFactory(WidgetDescriptor widgetDescriptor) {
        this.widgetDescriptor = widgetDescriptor;
    }

    @Override
    public Object getNewObject() {
        widgetModel = widgetDescriptor.getWidgetModel();
        return widgetModel;
    }

    @Override
    public Object getObjectType() {
        if (widgetModel == null) {
            widgetModel = widgetDescriptor.getWidgetModel();
        }
        Object widgetClass = widgetModel.getClass();
        return widgetClass;
    }

}
