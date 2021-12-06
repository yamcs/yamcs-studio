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

import org.csstudio.opibuilder.visualparts.MultiLineTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The property descriptor for multiline text editing.
 */
public class MultiLineTextPropertyDescriptor extends TextPropertyDescriptor {

    public MultiLineTextPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        setLabelProvider(new MultiLineLabelProvider());
    }

    @Override
    public CellEditor createPropertyEditor(final Composite parent) {
        final String title = NLS.bind("Edit {0}", getDisplayName());
        CellEditor editor = new MultiLineTextCellEditor(parent, title);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    static class MultiLineLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            return element == null ? "" : element.toString();
        }
    }
}
