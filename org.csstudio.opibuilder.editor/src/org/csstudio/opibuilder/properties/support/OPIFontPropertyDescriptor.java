/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.properties.support;

import org.csstudio.opibuilder.visualparts.OPIFontCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Descriptor for a property that has a value which should be edited with a font
 * cell editor.
 *
 * @author Xihui Chen
 *
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CellEditor createPropertyEditor(final Composite parent) {
        CellEditor editor = new OPIFontCellEditor(parent, "Choose Font");
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
