/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.figures.SpreadSheetTableFigure;
import org.csstudio.opibuilder.widgets.model.TableModel;
import org.csstudio.swt.widgets.natives.SpreadSheetTable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IActionFilter;

/**
 * EditPart of Table widget.
 */
public class TableEditPart extends AbstractBaseEditPart {

    private SpreadSheetTable spreadSheetTable;

    /**
     * The cell under mouse when menu is triggered. point.x is row index. poing.y is column index.
     */
    private Point menuTriggeredCell;

    private String[] allowedHeaders;

    @Override
    protected IFigure doCreateFigure() {
        var figure = new SpreadSheetTableFigure(this);
        spreadSheetTable = figure.getSWTWidget();
        spreadSheetTable.setEditable(getWidgetModel().isEditable());
        spreadSheetTable.setColumnsCount(getWidgetModel().getColumnsCount());
        spreadSheetTable.setColumnHeaders(getWidgetModel().getColumnHeaders());
        spreadSheetTable.setColumnWidths(getWidgetModel().getColumnWidthes());
        boolean editable[] = getWidgetModel().isColumnEditable();
        var columnCellEditorTypes = getWidgetModel().getColumnCellEditorTypes();
        for (var i = 0; i < Math.min(editable.length, spreadSheetTable.getColumnCount()); i++) {
            spreadSheetTable.setColumnEditable(i, editable[i]);
            spreadSheetTable.setColumnCellEditorType(i, columnCellEditorTypes[i]);
        }
        spreadSheetTable.setContent(getWidgetModel().getDefaultContent());

        spreadSheetTable.setColumnHeaderVisible(getWidgetModel().isColumnHeaderVisible());
        spreadSheetTable.getTableViewer().getTable().addMenuDetectListener(new MenuDetectListener() {

            @Override
            public void menuDetected(MenuDetectEvent e) {

                var index = spreadSheetTable
                        .getRowColumnIndex(spreadSheetTable.getTableViewer().getTable().toControl(e.x, e.y));
                if (index != null) {
                    menuTriggeredCell = new Point(index[0], index[1]);
                } else {
                    menuTriggeredCell = null;
                }
            }
        });

        return figure;
    }

    /**
     * Get the cell under mouse when menu is triggered.
     * 
     * @return the cell. point.x is row index. point.y is column index. null if no cell under mouse.
     */
    public Point getMenuTriggeredCell() {
        return menuTriggeredCell;
    }

    @Override
    public TableModel getWidgetModel() {
        return (TableModel) super.getWidgetModel();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class key) {
        if (key == IActionFilter.class) {
            return new BaseEditPartActionFilter() {
                @Override
                public boolean testAttribute(Object target, String name, String value) {
                    if (name.equals("allowInsert") && value.equals("TRUE")) {
                        return spreadSheetTable.isEditable()
                                && (getMenuTriggeredCell() != null || spreadSheetTable.isEmpty());
                    }
                    if (name.equals("allowDeleteRow") && value.equals("TRUE")) {
                        return spreadSheetTable.isEditable() && (getMenuTriggeredCell() != null);
                    }
                    if (name.equals("allowDeleteColumn") && value.equals("TRUE")) {
                        return spreadSheetTable.isEditable() && (getMenuTriggeredCell() != null
                                && spreadSheetTable.isColumnEditable(getMenuTriggeredCell().y));
                    }
                    return super.testAttribute(target, name, value);
                }
            };
        }
        return super.getAdapter(key);
    }

    @Override
    protected void registerPropertyChangeHandlers() {

        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                spreadSheetTable.setEditable((Boolean) newValue);
                return false;
            }
        };
        setPropertyChangeHandler(TableModel.PROP_EDITABLE, handler);

        handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                spreadSheetTable.setColumnHeaderVisible((Boolean) newValue);
                return false;
            }
        };
        setPropertyChangeHandler(TableModel.PROP_COLUMN_HEADER_VISIBLE, handler);

        IWidgetPropertyChangeHandler headersHandler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                var s = getWidgetModel().getColumnHeaders();
                spreadSheetTable.setColumnHeaders(s);
                var w = getWidgetModel().getColumnWidthes();
                spreadSheetTable.setColumnWidths(w);
                setPropertyValue(TableModel.PROP_COLUMNS_COUNT, s.length);
                getWidgetModel().updateContentPropertyTitles();
                return false;
            }
        };
        // update prop sheet immediately
        getWidgetModel().getProperty(TableModel.PROP_COLUMN_HEADERS)
                .addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        headersHandler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
                    }
                });

        handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                var headers = (String[][]) getPropertyValue(TableModel.PROP_COLUMN_HEADERS);
                if (headers.length > (Integer) newValue) {
                    var newHeaders = Arrays.copyOf(headers, (Integer) newValue);
                    setPropertyValue(TableModel.PROP_COLUMN_HEADERS, newHeaders);
                }
                spreadSheetTable.setColumnsCount((Integer) newValue);
                getWidgetModel().updateContentPropertyTitles();
                return false;
            }
        };
        setPropertyChangeHandler(TableModel.PROP_COLUMNS_COUNT, handler);

        handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                spreadSheetTable.setContent((String[][]) newValue);
                return false;
            }
        };
        setPropertyChangeHandler(TableModel.PROP_DEFAULT_CONTENT, handler);

    }

    /**
     * Get the native spread sheet table held by this widget.
     * 
     * @return the native spread sheet table.
     */
    public SpreadSheetTable getTable() {
        return spreadSheetTable;
    }

    /**
     * Set allowed header titles. If this is set, the insert column dialog will have a combo box instead of text for
     * title input.
     * 
     * @param headers
     *            the allowed header titles.
     */
    public void setAllowedHeaders(String[] headers) {
        this.allowedHeaders = headers;
    }

    public String[] getAllowedHeaders() {
        return allowedHeaders;
    }

}
