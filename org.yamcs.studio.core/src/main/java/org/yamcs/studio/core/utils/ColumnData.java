/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColumnData {

    private List<ColumnDef> columns = new ArrayList<>();

    private int visibleCount = 0;
    private int hiddenCount = 0;

    public void addColumn(String name, int width) {
        addColumn(name, width, true, true, true);
    }

    public void addColumn(String name, int width, boolean visible, boolean resizable, boolean moveable) {
        var column = new ColumnDef(name, columns.size());
        column.visible = visible;
        column.resizable = resizable;
        column.moveable = moveable;
        column.width = width;
        if (column.visible) {
            column.newIndex = visibleCount++;
        } else {
            column.newIndex = hiddenCount++;
        }
        columns.add(column);
    }

    public void clear() {
        columns.clear();
        visibleCount = 0;
        hiddenCount = 0;
    }

    public ColumnDef getColumn(String name) {
        for (var column : columns) {
            if (column.name.equals(name)) {
                return column;
            }
        }
        return null;
    }

    public List<ColumnDef> getColumns() {
        return columns;
    }

    public List<ColumnDef> getVisibleColumns() {
        return columns.stream().filter(c -> c.visible).collect(Collectors.toList());
    }

    public List<ColumnDef> getHiddenColumns() {
        return columns.stream().filter(c -> !c.visible).collect(Collectors.toList());
    }

    public void restore(String[] visibleColumnNames, String[] visibleWidths, String[] hiddenColumnNames) {
    }
}
