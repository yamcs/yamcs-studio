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

import org.csstudio.opibuilder.visualparts.PointListCellEditor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Descriptor for a property that has a value which should be edited with a pointlist cell editor.
 */
public class PointlistPropertyDescriptor extends TextPropertyDescriptor {
    /**
     * Creates an property descriptor with the given id and display name.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     * @param category
     *            the category
     */
    public PointlistPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
        setLabelProvider(new PointlistLabelProvider());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new PointListCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    /**
     * A label provider for multiple line Strings.
     */
    private final static class PointlistLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof PointList) {
                var list = (PointList) element;
                var buffer = new StringBuffer();
                if (list.size() > 0) {
                    this.addPointText(buffer, list.getPoint(0));
                    for (var i = 1; i < list.size(); i++) {
                        buffer.append("; ");
                        this.addPointText(buffer, list.getPoint(i));
                    }
                }
                return buffer.toString();
            } else {
                return element.toString();
            }
        }

        /**
         * Adds the text of the given Point to the StringBuffer.
         * 
         * @param buffer
         *            The StringBuffer
         * @param point
         *            The Point
         */
        private void addPointText(StringBuffer buffer, Point point) {
            buffer.append("(");
            buffer.append(point.x);
            buffer.append(",");
            buffer.append(point.y);
            buffer.append(")");
        }
    }
}
