/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.properties;

import org.csstudio.opibuilder.widgets.model.IntensityGraphModel;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The property descriptor for color map.
 */
public class ColorMapPropertyDescriptor extends TextPropertyDescriptor {

    private IntensityGraphModel intensityGraphModel;

    /**
     * Creates an property descriptor with the given id and display name.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     */
    public ColorMapPropertyDescriptor(Object id, String displayName, IntensityGraphModel intensityGraphModel) {
        super(id, displayName);
        this.intensityGraphModel = intensityGraphModel;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new ColorMapCellEditor(parent, "Edit Color Map", intensityGraphModel);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
