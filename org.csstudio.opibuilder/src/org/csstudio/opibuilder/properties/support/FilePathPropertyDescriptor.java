/********************************************************************************
 * Copyright (c) 2008 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties.support;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.visualparts.FilePathCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Descriptor for a property that has a value which should be edited with a path cell editor.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 * IPropertyDescriptor pd = new ResourcePropertyDescriptor(&quot;surname&quot;, &quot;Last Name&quot;);
 * </pre>
 *
 * </p>
 */
public class FilePathPropertyDescriptor extends TextPropertyDescriptor {

    /**
     * The accepted file extensions.
     */
    private String[] fileExtensions;

    private AbstractWidgetModel widgetModel;

    /**
     * Creates an property descriptor with the given id and display name.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     * @param widgetModel
     *            the widget model which contains the property of this descriptor.
     * @param fileExtensions
     *            The accepted file extensions
     */
    public FilePathPropertyDescriptor(Object id, String displayName, AbstractWidgetModel widgetModel,
            String[] fileExtensions) {
        super(id, displayName);
        this.fileExtensions = fileExtensions;
        this.widgetModel = widgetModel;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new FilePathCellEditor(parent, widgetModel, fileExtensions);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
