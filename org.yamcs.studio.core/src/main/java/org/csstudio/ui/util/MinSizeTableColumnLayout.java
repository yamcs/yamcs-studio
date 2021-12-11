/********************************************************************************
 * Copyright (c) 2014, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link TableColumnLayout} that enforces a minimum column width
 *
 * <p>
 * Especially on Linux this avoids the "vanishing column" issue when users resize a column to nothing and then moan
 * because it's gone.
 *
 * <p>
 * Table needs to be created as usual for TableColumnLayout, assigning a ColumnWeightData to each column for automated
 * resize.
 *
 * <p>
 * Once the user manually adjusts a column, the TableColumnLayout replaces the ColumnWeightData for automated resizing
 * with a ColumnPixelData to hold the exact user-selected width. The 'minimumWidth' of the original ColumnWeightData is
 * lost. This class holds its own min_width which is applied to all columns even after a user manually adjusted a
 * column.
 */
public class MinSizeTableColumnLayout extends TableColumnLayout {
    final private int min_width;

    /**
     * Initialize a TableColumnLayout that always enforces a minimum column size
     *
     * @param min_width
     *            Minimum column size in pixels
     */
    public MinSizeTableColumnLayout(int min_width) {
        this.min_width = min_width;
    }

    @Override
    protected void updateColumnData(Widget column) {
        var tColumn = (TableColumn) column;

        // Delegate to default implementation?
        if (tColumn.getWidth() > min_width) {
            super.updateColumnData(column);
        } else { // Enforce minimum width
            tColumn.setWidth(min_width);
            layout(tColumn.getParent().getParent(), true);
        }
    }
}
