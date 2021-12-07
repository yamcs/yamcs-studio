/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * The {@link LabelProvider} for the properties table.
 */
public class PropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 1 && element instanceof AbstractWidgetProperty) {
            var property = (AbstractWidgetProperty) element;

            if (property.isVisibleInPropSheet() && property.getPropertyDescriptor().getLabelProvider() != null) {
                return property.getPropertyDescriptor().getLabelProvider().getImage(property.getPropertyValue());
            }
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof AbstractWidgetProperty) {
            var property = (AbstractWidgetProperty) element;
            if (columnIndex == 0) {
                return property.getDescription();
            }

            if (property.isVisibleInPropSheet() && property.getPropertyDescriptor().getLabelProvider() != null) {
                return property.getPropertyDescriptor().getLabelProvider().getText(property.getPropertyValue());
            }
        }
        if (element != null) {
            return element.toString();
        }
        return "error";
    }

}
