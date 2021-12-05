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

import org.csstudio.opibuilder.visualparts.OPIFontCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Descriptor for a property that has a value which should be edited with a font cell editor.
 */
public final class OPIFontPropertyDescriptor extends TextPropertyDescriptor {
    /**
     * Standard constructor.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     */
    public OPIFontPropertyDescriptor(final Object id, final String displayName) {
        super(id, displayName);
    }

    @Override
    public CellEditor createPropertyEditor(final Composite parent) {
        CellEditor editor = new OPIFontCellEditor(parent, "Choose Font");
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
