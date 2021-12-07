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

import org.csstudio.opibuilder.visualparts.PVNameTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The property descriptor for PV Name which supports auto complete.
 */
public class PVNamePropertyDescriptor extends TextPropertyDescriptor {

    /**
     * @param id
     *            id of the property
     * @param displayName
     *            the display name in property sheet entry
     * @param detailedDescription
     *            the detailed description on tooltip and status line.
     */
    public PVNamePropertyDescriptor(Object id, String displayName, String detailedDescription) {
        super(id, displayName);
        setDescription(detailedDescription);
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {

        var editor = new PVNameTextCellEditor(parent);
        editor.getControl().setToolTipText(getDescription());
        return editor;
    }
}
