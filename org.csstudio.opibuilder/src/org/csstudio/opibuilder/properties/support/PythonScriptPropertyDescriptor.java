/*******************************************************************************
 * Copyright (c) 2022 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.properties.support;

import org.csstudio.opibuilder.visualparts.PythonScriptCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class PythonScriptPropertyDescriptor extends TextPropertyDescriptor {

    public PythonScriptPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        setLabelProvider(new PythonScriptLabelProvider());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        var title = NLS.bind("Edit {0}", getDisplayName());
        CellEditor editor = new PythonScriptCellEditor(parent, title);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    static class PythonScriptLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            return element == null ? "" : element.toString();
        }
    }
}
