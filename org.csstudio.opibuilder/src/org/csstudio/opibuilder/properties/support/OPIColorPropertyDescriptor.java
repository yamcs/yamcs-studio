/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties.support;

import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.visualparts.OPIColorCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * The property descriptor for OPI Color.
 */
public class OPIColorPropertyDescriptor extends PropertyDescriptor {

    public OPIColorPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        setLabelProvider(new OPIColorLabelProvider());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        var editor = new OPIColorCellEditor(parent, "Choose Color");
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    private final static class OPIColorLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            if (element != null && element instanceof OPIColor) {
                return ((OPIColor) element).getImage();
            }
            return null;
        }
    }

}
