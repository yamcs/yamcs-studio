/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.script.PVTuple;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * A table editor which can edit string boolean pair.
 */
public class PVTupleTableEditor extends Composite {

    private Action addAction;
    private Action removeAction;
    private Action moveUpAction;
    private Action moveDownAction;
    private Action checkTriggerAction;
    private Action uncheckTriggerAction;

    private TableViewer pvTupleListTableViewer;

    private List<PVTuple> pvTupleList;

    public PVTupleTableEditor(Composite parent, List<PVTuple> pvTupleList, int style) {
        super(parent, style);
        this.pvTupleList = pvTupleList;
        var gridLayout = new GridLayout(1, false);
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayoutData(gd);

        var toolbarManager = new ToolBarManager(SWT.FLAT);
        var toolBar = toolbarManager.createControl(this);
        var grid = new GridData();
        grid.horizontalAlignment = GridData.FILL;
        grid.verticalAlignment = GridData.BEGINNING;
        toolBar.setLayoutData(grid);

        createActions();
        toolbarManager.add(addAction);
        toolbarManager.add(removeAction);
        toolbarManager.add(moveUpAction);
        toolbarManager.add(moveDownAction);

        toolbarManager.update(true);

        var tableWrapper = new Composite(this, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        var tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        pvTupleListTableViewer = createPVTupleListTableViewer(tableWrapper, tcl);
        pvTupleListTableViewer.setInput(pvTupleList);

        // Context menu
        var menuManager = new MenuManager();
        menuManager.add(removeAction);
        menuManager.add(checkTriggerAction);
        menuManager.add(uncheckTriggerAction);
        var contextMenu = menuManager.createContextMenu(pvTupleListTableViewer.getTable());
        pvTupleListTableViewer.getTable().setMenu(contextMenu);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        pvTupleListTableViewer.getControl().setEnabled(enabled);
    }

    public void updateInput(List<PVTuple> new_items) {
        pvTupleList = new_items;
        pvTupleListTableViewer.setInput(new_items);
        pvTupleListTableViewer.setLabelProvider(new PVTupleLabelProvider(pvTupleList));
    }

    /**
     * Creates and configures a {@link TableViewer}.
     *
     * @param parent
     *            The parent for the table
     * @return The {@link TableViewer}
     */
    private TableViewer createPVTupleListTableViewer(Composite parent, TableColumnLayout tcl) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        var numColumn = new TableViewerColumn(viewer, SWT.NONE);
        numColumn.getColumn().setText("#");
        tcl.setColumnData(numColumn.getColumn(), new ColumnPixelData(50));

        var pvColumn = new TableViewerColumn(viewer, SWT.NONE);
        pvColumn.getColumn().setText("PV Name");
        tcl.setColumnData(pvColumn.getColumn(), new ColumnWeightData(50));
        pvColumn.setEditingSupport(new PVColumnEditingSupport(viewer, viewer.getTable()));

        var trigColumn = new TableViewerColumn(viewer, SWT.NONE);
        trigColumn.getColumn().setText("Trigger");
        tcl.setColumnData(trigColumn.getColumn(), new ColumnPixelData(50));
        trigColumn.setEditingSupport(new TriggerColumnEditingSupport(viewer, viewer.getTable()));

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new PVTupleLabelProvider(pvTupleList));

        viewer.addSelectionChangedListener(event -> refreshToolbarOnSelection());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        var target = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_COPY);
        target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
        target.addDropListener(new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetEvent event) {
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event) {
            }

            @Override
            public void dragOver(DropTargetEvent event) {
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (event == null || !(event.data instanceof String)) {
                    return;
                }

                var txt = (String) event.data;
                var names = txt.split("[\r\n]+");
                var tuples = new PVTuple[names.length];
                var i = 0;
                for (var name : names) {
                    tuples[i] = new PVTuple(name, true);
                    pvTupleList.add(tuples[i]);
                    i++;
                }

                refreshTableViewerFromAction(tuples);
            }

            @Override
            public void dropAccept(DropTargetEvent event) {
            }
        });

        return viewer;
    }

    /**
     * Refreshes the enabled-state of the actions.
     */
    private void refreshToolbarOnSelection() {

        var selection = (IStructuredSelection) pvTupleListTableViewer.getSelection();

        var num_tuple = 0;
        for (var obj : selection.toArray()) {
            if (obj instanceof PVTuple) {
                num_tuple++;
            }
        }

        if (num_tuple == 0) {
            removeAction.setEnabled(false);
            moveUpAction.setEnabled(false);
            moveDownAction.setEnabled(false);
            checkTriggerAction.setEnabled(false);
            uncheckTriggerAction.setEnabled(false);
        } else if (num_tuple == 1) {
            removeAction.setEnabled(true);
            moveUpAction.setEnabled(true);
            moveDownAction.setEnabled(true);
            checkTriggerAction.setEnabled(true);
            uncheckTriggerAction.setEnabled(true);
        } else {
            removeAction.setEnabled(true);
            moveUpAction.setEnabled(false);
            moveDownAction.setEnabled(false);
            checkTriggerAction.setEnabled(true);
            uncheckTriggerAction.setEnabled(true);
        }
    }

    /**
     * @param tuple
     *            the tuple to be selected
     */
    private void refreshTableViewerFromAction(PVTuple[] tuples) {
        pvTupleListTableViewer.refresh();
        if (tuples == null || tuples.length == 0) {
            pvTupleListTableViewer.setSelection(StructuredSelection.EMPTY);
        } else if (tuples.length == 1) {
            pvTupleListTableViewer.setSelection(new StructuredSelection(tuples[0]), true);
        } else {
            pvTupleListTableViewer.setSelection(new StructuredSelection(tuples), true);
        }
    }

    private void createActions() {
        addAction = new Action("Add") {
            @Override
            public void run() {
                var tuple = new PVTuple("", true);
                pvTupleList.add(tuple);
                refreshTableViewerFromAction(new PVTuple[] { tuple });
            }
        };
        addAction.setToolTipText("Add a PV");
        addAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/add.gif"));

        removeAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) pvTupleListTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    var iter = selection.iterator();
                    while (iter.hasNext()) {
                        var item = iter.next();
                        if (item instanceof PVTuple) {
                            pvTupleList.remove(item);
                        }
                    }

                    refreshTableViewerFromAction(null);
                    super.setEnabled(false);
                }
            }
        };
        removeAction.setText("Remove");
        removeAction.setToolTipText("Remove the selected PVs from the list");
        removeAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/delete.gif"));
        removeAction.setEnabled(false);

        moveUpAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) pvTupleListTableViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof PVTuple) {
                    var tuple = (PVTuple) selection.getFirstElement();
                    var i = pvTupleList.indexOf(tuple);
                    if (i > 0) {
                        pvTupleList.remove(tuple);
                        pvTupleList.add(i - 1, tuple);
                        refreshTableViewerFromAction(new PVTuple[] { tuple });
                    }
                }
            }
        };
        moveUpAction.setText("Move Up");
        moveUpAction.setToolTipText("Move up the selected PV");
        moveUpAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_prev.gif"));
        moveUpAction.setEnabled(false);

        moveDownAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) pvTupleListTableViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof PVTuple) {
                    var tuple = (PVTuple) selection.getFirstElement();
                    var i = pvTupleList.indexOf(tuple);
                    if (i < pvTupleList.size() - 1) {
                        pvTupleList.remove(tuple);
                        pvTupleList.add(i + 1, tuple);
                        refreshTableViewerFromAction(new PVTuple[] { tuple });
                    }
                }
            }
        };
        moveDownAction.setText("Move Down");
        moveDownAction.setToolTipText("Move down the selected PV");
        moveDownAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_next.gif"));
        moveDownAction.setEnabled(false);

        checkTriggerAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) pvTupleListTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    var iter = selection.iterator();
                    var tuples = new ArrayList<PVTuple>();
                    while (iter.hasNext()) {
                        var item = iter.next();
                        if (item instanceof PVTuple) {
                            var tuple = (PVTuple) item;
                            tuple.trigger = true;
                            tuples.add(tuple);
                        }
                    }

                    refreshTableViewerFromAction(tuples.toArray(new PVTuple[tuples.size()]));
                }
            }
        };
        checkTriggerAction.setText("Check Trigger");
        checkTriggerAction.setToolTipText("Check trigger option of the selected PVs");
        checkTriggerAction.setEnabled(false);

        uncheckTriggerAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) pvTupleListTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    var iter = selection.iterator();
                    var tuples = new ArrayList<PVTuple>();
                    while (iter.hasNext()) {
                        var item = iter.next();
                        if (item instanceof PVTuple) {
                            var tuple = (PVTuple) item;
                            tuple.trigger = false;
                            tuples.add(tuple);
                        }
                    }

                    refreshTableViewerFromAction(tuples.toArray(new PVTuple[tuples.size()]));
                }
            }
        };
        uncheckTriggerAction.setText("Uncheck Trigger");
        uncheckTriggerAction.setToolTipText("Uncheck trigger option of the selected PVs");
        uncheckTriggerAction.setEnabled(false);
    }

    private final static class PVTupleLabelProvider extends LabelProvider implements ITableLabelProvider {

        private List<PVTuple> pvTupleList;

        public PVTupleLabelProvider(List<PVTuple> pvTupleList) {
            this.pvTupleList = pvTupleList;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 2 && element instanceof PVTuple) {
                if (((PVTuple) element).trigger) {
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
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return String.valueOf(pvTupleList.indexOf(element));
            }
            if (columnIndex == 1 && element instanceof PVTuple) {
                return ((PVTuple) element).pvName;
            }
            if (columnIndex == 2 && element instanceof PVTuple) {
                return ((PVTuple) element).trigger ? "yes" : "no";
            }
            return null;
        }
    }

    private final static class PVColumnEditingSupport extends EditingSupport {

        private Table table;

        public PVColumnEditingSupport(ColumnViewer viewer, Table table) {
            super(viewer);
            this.table = table;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new PVNameTextCellEditor(table);
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof PVTuple) {
                return ((PVTuple) element).pvName;
            }
            return null;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof PVTuple) {
                var s = value == null ? "" : value.toString();
                ((PVTuple) element).pvName = s;
                getViewer().refresh();
            }
        }
    }

    private final static class TriggerColumnEditingSupport extends EditingSupport {

        private Table table;

        public TriggerColumnEditingSupport(ColumnViewer viewer, Table table) {
            super(viewer);
            this.table = table;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new CheckboxCellEditor(table);
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof PVTuple) {
                return ((PVTuple) element).trigger;
            }
            return null;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof PVTuple) {
                ((PVTuple) element).trigger = (Boolean) value;
                getViewer().refresh();
            }
        }
    }
}
