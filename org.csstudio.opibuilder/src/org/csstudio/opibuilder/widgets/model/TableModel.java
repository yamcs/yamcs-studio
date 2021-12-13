/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.StringTableProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.ui.util.swt.stringtable.StringTableEditor.CellEditorType;

/**
 * Model for the Table widget.
 */
public class TableModel extends AbstractWidgetModel {

    private static final int DEFAULT_COLUMN_WIDTH = 60;

    /**
     * True if the table cell is editable. If false, it is still selectable, which is different with disabled.
     */
    public static final String PROP_EDITABLE = "editable";

    /**
     * Column headers.
     */
    public static final String PROP_COLUMN_HEADERS = "column_headers";

    /**
     * Number of columns.
     */
    public static final String PROP_COLUMNS_COUNT = "columns_count";

    /**
     * Default Content of the table.
     */
    public static final String PROP_DEFAULT_CONTENT = "default_content";

    /**
     * Column header visible.
     */
    public static final String PROP_COLUMN_HEADER_VISIBLE = "column_header_visible";

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.table";

    @Override
    protected void configureProperties() {

        addProperty(new BooleanProperty(PROP_EDITABLE, "Editable", WidgetPropertyCategory.Behavior, true));

        var contentProperty = new StringTableProperty(PROP_DEFAULT_CONTENT, "Default Content",
                WidgetPropertyCategory.Display, new String[][] { { "" } }, new String[] { "" });

        addProperty(contentProperty);

        var dropDownOptions = new String[org.csstudio.swt.widgets.natives.SpreadSheetTable.CellEditorType
                .values().length];
        for (var i = 0; i < dropDownOptions.length; i++) {
            dropDownOptions[i] = org.csstudio.swt.widgets.natives.SpreadSheetTable.CellEditorType.values()[i].name();
        }

        var headersProperty = new StringTableProperty(PROP_COLUMN_HEADERS, "Column Headers",
                WidgetPropertyCategory.Display, new String[0][0],
                new String[] { "Column Title", "Column Width", "Editable", "CellEditor" },
                new CellEditorType[] { CellEditorType.TEXT, CellEditorType.TEXT, CellEditorType.CHECKBOX,
                        CellEditorType.DROPDOWN },
                new Object[] { null, null, new String[] { "Yes", "No" }, dropDownOptions });

        addProperty(headersProperty);

        var columnsCountProperty = new IntegerProperty(PROP_COLUMNS_COUNT, "Columns Count",
                WidgetPropertyCategory.Display, 1, 1, 10000);

        addProperty(columnsCountProperty);

        addProperty(new BooleanProperty(PROP_COLUMN_HEADER_VISIBLE, "Column Header Visible",
                WidgetPropertyCategory.Display, true));

        headersProperty.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateContentPropertyTitles();
            }
        });

        columnsCountProperty.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateContentPropertyTitles();
            }
        });
    }

    public void updateContentPropertyTitles() {

        var headers = getColumnHeaders();
        var c = getColumnsCount();
        if (headers.length > c) {
            c = headers.length;
        }

        var titles = new String[c];

        for (var i = 0; i < titles.length; i++) {
            if (i < headers.length) {
                titles[i] = headers[i];
            } else {
                titles[i] = "";
            }
        }
        ((StringTableProperty) getProperty(PROP_DEFAULT_CONTENT)).setTitles(titles);
    }

    public boolean isEditable() {
        return (Boolean) getPropertyValue(PROP_EDITABLE);
    }

    public boolean[] isColumnEditable() {
        var headers = (String[][]) getPropertyValue(PROP_COLUMN_HEADERS);
        var r = new boolean[headers.length];
        if (headers.length == 0 || headers[0].length < 3) {
            Arrays.fill(r, true);
            return r;
        }
        for (var i = 0; i < headers.length; i++) {
            r[i] = headers[i][2].toLowerCase().equals("no") ? false : true;
        }
        return r;
    }

    public org.csstudio.swt.widgets.natives.SpreadSheetTable.CellEditorType[] getColumnCellEditorTypes() {
        var headers = (String[][]) getPropertyValue(PROP_COLUMN_HEADERS);
        var r = new org.csstudio.swt.widgets.natives.SpreadSheetTable.CellEditorType[headers.length];
        if (headers.length == 0 || headers[0].length < 4) {
            Arrays.fill(r, org.csstudio.swt.widgets.natives.SpreadSheetTable.CellEditorType.TEXT);
            return r;
        }
        for (var i = 0; i < headers.length; i++) {
            try {
                r[i] = org.csstudio.swt.widgets.natives.SpreadSheetTable.CellEditorType.valueOf(headers[i][3]);
            } catch (Exception e) {
                r[i] = org.csstudio.swt.widgets.natives.SpreadSheetTable.CellEditorType.TEXT;
            }
        }
        return r;
    }

    public String[] getColumnHeaders() {
        var headers = (String[][]) getPropertyValue(PROP_COLUMN_HEADERS);
        var r = new String[headers.length];
        for (var i = 0; i < headers.length; i++) {
            r[i] = headers[i][0];
        }
        return r;
    }

    public int[] getColumnWidthes() {
        var headers = (String[][]) getPropertyValue(PROP_COLUMN_HEADERS);
        var r = new int[headers.length];
        for (var i = 0; i < headers.length; i++) {
            try {
                r[i] = Integer.valueOf(headers[i][1]);
            } catch (Exception e) {
                r[i] = DEFAULT_COLUMN_WIDTH;
            }
        }
        return r;
    }

    public int getColumnsCount() {
        return (Integer) getPropertyValue(PROP_COLUMNS_COUNT);
    }

    public String[][] getDefaultContent() {
        return (String[][]) getPropertyValue(PROP_DEFAULT_CONTENT);
    }

    public boolean isColumnHeaderVisible() {
        return (Boolean) getPropertyValue(PROP_COLUMN_HEADER_VISIBLE);
    }

    @Override
    public String getTypeID() {
        return ID;
    }
}
