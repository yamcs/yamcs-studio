/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class OPIShellsTableViewer extends TableViewer {

    private static final String COL_PATH = "Path";
    private static final String COL_TITLE = "Title";

    public OPIShellsTableViewer(Composite parent, TableColumnLayout tcl) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);

        var pathColumn = new TableViewerColumn(this, SWT.LEFT);
        pathColumn.getColumn().setText(COL_PATH);
        pathColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var opiShell = (OPIShell) element;
                return opiShell.getPath().toString();
            }
        });
        tcl.setColumnData(pathColumn.getColumn(), new ColumnWeightData(200));

        var titleColumn = new TableViewerColumn(this, SWT.LEFT);
        titleColumn.getColumn().setText(COL_TITLE);
        titleColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var opiShell = (OPIShell) element;
                return opiShell.getTitle();
            }
        });
        tcl.setColumnData(titleColumn.getColumn(), new ColumnWeightData(200));

        setContentProvider(ArrayContentProvider.getInstance());
        setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                var s1 = (OPIShell) o1;
                var s2 = (OPIShell) o2;
                return s1.getPath().toString().compareToIgnoreCase(s2.getPath().toString());
            }
        });

        addPopupMenu();

        addDoubleClickListener(evt -> {
            var sel = getTable().getSelection();
            for (var item : sel) {
                var shell = (OPIShell) item.getData();
                shell.raiseToTop();
            }
        });
    }

    private void addPopupMenu() {
        var contextMenu = new Menu(getTable());
        getTable().setMenu(contextMenu);
        var mItem1 = new MenuItem(contextMenu, SWT.NONE);
        mItem1.setText("Show OPI");
        mItem1.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                var sel = getTable().getSelection();
                for (var item : sel) {
                    var shell = (OPIShell) item.getData();
                    shell.raiseToTop();
                }
            }
        });

        var mItem2 = new MenuItem(contextMenu, SWT.NONE);
        mItem2.setText("Close OPI");
        mItem2.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                var sel = getTable().getSelection();
                for (var item : sel) {
                    var shell = (OPIShell) item.getData();
                    shell.close();
                }
            }
        });
    }
}
