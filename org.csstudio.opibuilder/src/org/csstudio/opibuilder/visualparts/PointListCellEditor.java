/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/**
 * A table cell editor for values of type PointList.
 */
public final class PointListCellEditor extends AbstractDialogCellEditor {

    /**
     * The current PointList value.
     */
    private List<Point> _pointList;
    /**
     * A copy of the current PointList value.
     */
    private List<Point> _orgPointList;

    /**
     * Creates a new string cell editor parented under the given control. The cell editor value is a PointList.
     */
    public PointListCellEditor(Composite parent) {
        super(parent, "Points");
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new PointListInputDialog(parentShell, dialogTitle);
        if (dialog.open() == Window.CANCEL) {
            _pointList = _orgPointList;
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        if (_pointList.size() == _orgPointList.size()) {
            for (var i = 0; i < _pointList.size(); i++) {
                var p1 = _pointList.get(i);
                var p2 = _orgPointList.get(i);
                if (p1.x != p2.x || p1.y != p2.y) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    protected Object doGetValue() {
        return this.listToPointList(_pointList);
    }

    /**
     * Creates a new PointList with the Points of the given List.
     * 
     * @param list
     *            A {@link List} which contains the Points
     * @return PointList The new {@link PointList}
     */
    private PointList listToPointList(List<Point> list) {
        var result = new PointList();
        for (var p : list) {
            result.addPoint(p);
        }
        return result;
    }

    @Override
    protected void doSetValue(Object value) {
        Assert.isTrue(value instanceof PointList);
        var list = (PointList) value;
        _orgPointList = new LinkedList<>();
        _pointList = new LinkedList<>();
        for (var i = 0; i < list.size(); i++) {
            _pointList.add(list.getPoint(i));
            _orgPointList.add(list.getPoint(i));
        }
    }

    /**
     * This class represents a Dialog to add, edit and remove Points of a PointList.
     */
    private final class PointListInputDialog extends Dialog {

        private String _title;
        private ListViewer _viewer;
        private Action _addAction;
        private Action _editAction;
        private Action _removeAction;
        private Action _upAction;
        private Action _downAction;

        public PointListInputDialog(Shell parentShell, String dialogTitle) {
            super(parentShell);
            _title = dialogTitle;
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            if (_title != null) {
                shell.setText(_title);
            }
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            var parentComposite = (Composite) super.createDialogArea(parent);

            var topComposite = new Composite(parentComposite, SWT.NONE);
            var gl = new GridLayout();
            gl.marginHeight = 0;
            gl.marginWidth = 0;
            gl.horizontalSpacing = 0;
            topComposite.setLayout(gl);

            var gd = new GridData(GridData.FILL_BOTH);
            topComposite.setLayoutData(gd);

            var toolbarManager = new ToolBarManager(SWT.FLAT);
            var toolBar = toolbarManager.createControl(topComposite);
            var gid = new GridData();
            gid.horizontalAlignment = GridData.FILL;
            gid.verticalAlignment = GridData.BEGINNING;
            createActions(toolbarManager);
            toolBar.setLayoutData(gid);

            _viewer = createListViewer(topComposite);
            hookPopupMenu(_viewer);
            hookDoubleClick(_viewer);

            return parentComposite;
        }

        private void createActions(ToolBarManager manager) {
            _addAction = new Action() {
                @Override
                public void run() {
                    openPointDialog(true);
                }
            };
            _addAction.setText("Add " + _title);
            _addAction.setToolTipText("Adds a new " + _title + " to the list");
            _addAction.setImageDescriptor(CustomMediaFactory.getInstance()
                    .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/add.gif"));
            manager.add(_addAction);
            _editAction = new Action() {
                @Override
                public void run() {
                    openPointDialog(false);
                    refreshActions();
                }
            };
            _editAction.setText("Edit " + _title);
            _editAction.setToolTipText("Edits the selected " + _title);
            _editAction.setImageDescriptor(CustomMediaFactory.getInstance()
                    .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/edit.gif"));
            _editAction.setEnabled(false);
            manager.add(_editAction);
            _removeAction = new Action() {
                @Override
                public void run() {
                    removePoint();
                    refreshActions();
                }
            };
            _removeAction.setText("Remove " + _title);
            _removeAction.setToolTipText("Removes the selected " + _title + " from the list");
            _removeAction.setImageDescriptor(CustomMediaFactory.getInstance()
                    .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/delete.gif"));
            _removeAction.setEnabled(false);
            manager.add(_removeAction);
            manager.add(new Separator());
            _upAction = new Action() {
                @Override
                public void run() {
                    movePoint(true);
                }
            };
            _upAction.setText("Move up");
            _upAction.setToolTipText("Increases the index of the selected Point");
            _upAction.setImageDescriptor(CustomMediaFactory.getInstance()
                    .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_prev.gif"));
            _upAction.setEnabled(false);
            manager.add(_upAction);
            _downAction = new Action() {
                @Override
                public void run() {
                    movePoint(false);
                }
            };
            _downAction.setText("Move down");
            _downAction.setToolTipText("Decreases the index of the selected Point");
            _downAction.setImageDescriptor(CustomMediaFactory.getInstance()
                    .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_next.gif"));
            _downAction.setEnabled(false);
            manager.add(_downAction);
            manager.update(true);
        }

        private ListViewer createListViewer(Composite parent) {
            var viewer = new ListViewer(parent);
            viewer.setContentProvider(new ArrayContentProvider());
            viewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    if (element instanceof Point) {
                        var p = (Point) element;
                        return p.toString();// p.x+","+p.y;
                    }
                    return element.toString();
                }
            });
            viewer.setInput(_pointList.toArray(new Point[_pointList.size()]));
            var gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
            gridData.verticalSpan = 6;
            gridData.heightHint = 150;
            viewer.getList().setLayoutData(gridData);
            viewer.getList().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    refreshActions();
                }
            });
            viewer.getList().setFocus();
            return viewer;
        }

        /**
         * Adds a Popup menu to the given ListViewer.
         */
        private void hookPopupMenu(ListViewer viewer) {
            var popupMenu = new MenuManager();
            popupMenu.add(_addAction);
            popupMenu.add(_editAction);
            popupMenu.add(new Separator());
            popupMenu.add(_removeAction);
            popupMenu.add(new Separator());
            popupMenu.add(_upAction);
            popupMenu.add(_downAction);
            var menu = popupMenu.createContextMenu(viewer.getList());
            viewer.getList().setMenu(menu);
        }

        /**
         * Adds doubleclick support to the given ListViewer.
         */
        private void hookDoubleClick(ListViewer viewer) {
            viewer.getControl().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    if (_viewer.getList().getSelectionCount() == 1) {
                        _editAction.run();
                    } else {
                        _addAction.run();
                    }
                }
            });
        }

        /**
         * Opens a Dialog for adding a new Point.
         * 
         * @param isNew
         *            True, if a new Point should be created, false otherwise
         */
        private void openPointDialog(boolean isNew) {
            var index = _viewer.getList().getItemCount();
            var selectedIndices = _viewer.getList().getSelectionIndices();
            if (selectedIndices.length > 0) {
                index = selectedIndices[0];
            }
            var dialog = new PointDialog(this.getParentShell(), "Point", null, index, isNew);
            if (dialog.open() == Window.OK) {
                setInput();
            }
            _viewer.getList().setSelection(index);
            refreshActions();
        }

        /**
         * Removes the current selected Points from the List.
         */
        private void removePoint() {
            if (_viewer.getList().getSelectionIndices().length > 0) {
                var selectedIndices = _viewer.getList().getSelectionIndices();
                Arrays.sort(selectedIndices);
                var i = 0;
                for (var s : selectedIndices) {
                    _pointList.remove(s - i);
                    i++;
                }
                setInput();
            }
            refreshActions();
        }

        /**
         * Enables or disables the RemoveButton.
         */
        private void refreshActions() {
            if (_viewer.getList().getItemCount() > 2) {
                _removeAction.setEnabled(_viewer.getList().getSelectionIndices().length > 0);
            } else {
                _removeAction.setEnabled(false);
            }
            _editAction.setEnabled(_viewer.getList().getSelectionIndices().length == 1);
            _upAction.setEnabled(
                    _viewer.getList().getSelectionIndices().length == 1 && _viewer.getList().getSelectionIndex() > 0);
            _downAction.setEnabled(_viewer.getList().getSelectionIndices().length == 1
                    && _viewer.getList().getSelectionIndex() < _viewer.getList().getItemCount() - 1);
        }

        /**
         * Moves the current selected Point one step up or down, depending on the given boolean.
         * 
         * @param up
         *            True, if the Point should be moved up, false otherwise
         */
        private void movePoint(boolean up) {
            var newIndex = _viewer.getList().getSelectionIndex();
            var point = _pointList.get(newIndex);
            _pointList.remove(newIndex);
            if (up) {
                newIndex = newIndex - 1;
            } else {
                newIndex = newIndex + 1;
            }
            if (newIndex < 0) {
                newIndex = 0;
            }
            if (newIndex > _pointList.size()) {
                newIndex = _pointList.size();
            }
            _pointList.add(newIndex, point);
            setInput();
            _viewer.getList().setSelection(newIndex);
            refreshActions();
        }

        /**
         * Sets the input on the viewer and refreshes it.
         */
        private void setInput() {
            _viewer.setInput(_pointList.toArray(new Point[_pointList.size()]));
            _viewer.refresh();
        }
    }

    /**
     * This class represents a Dialog for editing a Point.
     */
    private final class PointDialog extends Dialog {
        /**
         * The title of the dialog.
         */
        private String _title;
        /**
         * The message to display, or <code>null</code> if none.
         */
        private String _message;
        /**
         * The input value; the empty string by default.
         */
        private int _index = -1;
        /**
         * The Spinner for the x-value of the Point.
         */
        private Spinner _xSpinner;
        /**
         * The Spinner for the y-value of the Point.
         */
        private Spinner _ySpinner;
        /**
         * A boolean, which indicates if the Point is new.
         */
        private boolean _isNew;

        /**
         * Creates an input dialog with OK and Cancel buttons. Note that the dialog will have no visual representation
         * (no widgets) until it is told to open.
         * <p>
         * Note that the <code>open</code> method blocks for input dialogs.
         * </p>
         *
         * @param parentShell
         *            the parent shell, or <code>null</code> to create a top-level shell
         * @param dialogTitle
         *            the dialog title, or <code>null</code> if none
         * @param dialogMessage
         *            the dialog message, or <code>null</code> if none
         * @param initialValue
         *            the initial input value, or <code>null</code> if none
         * @param isNew
         *            true id the Point is new, false otherwise
         */
        public PointDialog(Shell parentShell, String dialogTitle, String dialogMessage, int initialValue,
                boolean isNew) {
            super(parentShell);
            _title = dialogTitle;
            _message = dialogMessage;
            _isNew = isNew;
            if (initialValue >= 0) {
                _index = initialValue;
            }
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            if (_title != null) {
                shell.setText(_title);
            }
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            var composite = (Composite) super.createDialogArea(parent);
            composite.setLayout(new GridLayout(2, false));
            if (_message != null) {
                var label = new Label(composite, SWT.WRAP);
                label.setText(_message);
                var data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                        | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
                data.horizontalSpan = 2;
                data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
                label.setLayoutData(data);
            }
            var label = new Label(composite, SWT.NONE);
            label.setText("x:");
            _xSpinner = new Spinner(composite, SWT.BORDER);
            _xSpinner.setMaximum(10000);
            _xSpinner.setMinimum(-10000);
            if (_index < 0 || _index >= _pointList.size()) {
                _xSpinner.setSelection(0);
            } else {
                _xSpinner.setSelection(_pointList.get(_index).x);
            }
            label = new Label(composite, SWT.NONE);
            label.setText("y:");
            _ySpinner = new Spinner(composite, SWT.BORDER);
            _ySpinner.setMaximum(10000);
            _ySpinner.setMinimum(-10000);
            if (_index < 0 || _index >= _pointList.size()) {
                _ySpinner.setSelection(0);
            } else {
                _ySpinner.setSelection(_pointList.get(_index).y);
            }
            return composite;
        }

        @Override
        protected void okPressed() {
            this.getButton(IDialogConstants.OK_ID).setFocus();
            if (_index >= 0) {
                if (!_isNew) {
                    _pointList.remove(_index);
                }
                _pointList.add(_index, new Point(_xSpinner.getSelection(), _ySpinner.getSelection()));
            }
            // _pointList.add(new Point(_xSpinner.getSelection(), _ySpinner.getSelection()));
            super.okPressed();
        }
    }
}
