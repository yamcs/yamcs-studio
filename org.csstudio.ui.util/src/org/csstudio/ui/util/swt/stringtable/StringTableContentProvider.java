/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.swt.stringtable;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for List of String or String[].
 * <p>
 * Will provide Integer index values which allow the label provider and editor to access the correct elements in the
 * List.
 * 
 * @param <T>
 *            String or String[]
 */
class StringTableContentProvider<T> implements IStructuredContentProvider {
    /** Magic number for the final 'add' element */
    final public static Integer ADD_ELEMENT = new Integer(-1);
    private List<T> items;

    @Override
    @SuppressWarnings("unchecked")
    public void inputChanged(Viewer viewer, Object old, Object new_input) {
        items = (List<T>) new_input;
    }

    @Override
    public Object[] getElements(Object arg0) {
        var N = items.size();
        Integer result[] = new Integer[N + 1];
        for (var i = 0; i < N; ++i) {
            result[i] = i;
        }
        result[N] = ADD_ELEMENT;
        return result;
    }

    @Override
    public void dispose() {
        // NOP
    }
}
