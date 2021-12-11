/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.swt.stringtable;

import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * Label provider that transforms Integer index into list into the string to display.
 */
class StringColumnLabelProvider extends CellLabelProvider {
    final private TableViewer viewer;

    /**
     * Initialize
     * 
     * @param items
     *            It
     */
    public StringColumnLabelProvider(TableViewer viewer) {
        this.viewer = viewer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(ViewerCell cell) {
        var items = (List<String>) viewer.getInput();
        var index = ((Integer) cell.getElement()).intValue();
        if (index < 0) {
            cell.setText("<Add>");
        } else {
            cell.setText(items.get(index));
        }
    }
}
