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
 * Label provider that transforms Integer index into corresponding string in table data to display.
 */
class StringMultiColumnsLabelProvider extends CellLabelProvider {
    final private TableViewer tableViewer;
    final private boolean editable;

    public StringMultiColumnsLabelProvider(TableViewer tableViewer, boolean editable) {
        this.tableViewer = tableViewer;
        this.editable = editable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(ViewerCell cell) {
        var items = (List<String[]>) tableViewer.getInput();
        var index = ((Integer) cell.getElement()).intValue();
        // if this is the extra row
        if (index < 0) {
            if (editable) {
                cell.setText("<Add>");
            } else {
                cell.setText("");
                // if not
            }
        } else {
            // For multi-line text, only show the first line
            var column = cell.getColumnIndex();
            var text = "";
            if (column < items.get(index).length) {
                text = items.get(index)[column];
            }
            // Not sure whether to look for '\r' or '\n'. Try both
            var nl = text.indexOf('\r');
            if (nl < 0) {
                nl = text.indexOf('\n');
            }
            if (nl > 0) {
                text = text.substring(0, nl) + "...";
            }
            cell.setText(text);
        }
    }
}
