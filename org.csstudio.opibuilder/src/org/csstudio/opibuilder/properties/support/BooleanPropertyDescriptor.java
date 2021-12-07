/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties.support;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Descriptor for a property that is a boolean value which should be edited with a boolean cell editor.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 * IPropertyDescriptor pd = new BooleanPropertyDescriptor(&quot;fg&quot;, &quot;boolean&quot;);
 * </pre>
 *
 * </p>
 */
public final class BooleanPropertyDescriptor extends PropertyDescriptor {

    /**
     * Creates an property descriptor with the given id and display name.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     */
    public BooleanPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);

        setLabelProvider(new BooleanLabelProvider());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new CheckboxCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    /**
     * A label provider for boolean value, which displays a checked or unchecked box image.
     */
    private final static class BooleanLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            if (element instanceof Boolean) {
                if (((Boolean) element).booleanValue()) {
                    return CustomMediaFactory.getInstance().getImageFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                            "icons/checked.gif");
                } else {
                    return CustomMediaFactory.getInstance().getImageFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                            "icons/unchecked.gif");
                }
            } else {
                return null;
            }
        }

        @Override
        public String getText(Object element) {
            if (element instanceof Boolean) {
                if (((Boolean) element).booleanValue()) {
                    return "yes";
                } else {
                    return "no";
                }
            } else {
                return element.toString();
            }
        }
    }
}
