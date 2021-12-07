/********************************************************************************
 * Copyright (c) 2010, 2021 IBM Corporation and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.core.utils;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class ViewerColumnsDialog extends ViewerSettingsAndStatusDialog {

    private List<ColumnDef> visible;
    private List<ColumnDef> nonVisible;

    private TableViewer visibleViewer;
    private TableViewer nonVisibleViewer;

    private Button upButton;
    private Button downButton;

    private Button toVisibleButton;
    private Button toNonVisibleButton;

    private Label widthLabel;
    private Text widthText;

    private Point tableLabelSize;

    public ViewerColumnsDialog(Shell parentShell, ColumnData columnData) {
        super(parentShell);
        visible = columnData.getVisibleColumns();
        nonVisible = columnData.getHiddenColumns();
    }

    @Override
    protected Control createDialogContentArea(Composite parent) {
        var composite = new Composite(parent, SWT.NONE);
        var gridLayout = new GridLayout(4, false);
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createInvisibleTable(composite);
        createMoveButtons(composite);
        createVisibleTable(composite);
        createUpDownBtt(composite);
        createWidthArea(composite);
        var element = visibleViewer.getElementAt(0);
        if (element != null) {
            visibleViewer.setSelection(new StructuredSelection(element));
        }
        visibleViewer.getTable().setFocus();
        return composite;
    }

    /**
     * The Up and Down button to change column ordering.
     */
    private Control createUpDownBtt(Composite parent) {
        var composite = new Composite(parent, SWT.NONE);
        var compositeLayout = new GridLayout();
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        composite.setLayout(compositeLayout);
        composite.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));

        var bttArea = new Composite(composite, SWT.NONE);
        var layout = new GridLayout();
        layout.marginHeight = 0;
        bttArea.setLayout(layout);
        bttArea.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, true));
        upButton = new Button(bttArea, SWT.PUSH);
        upButton.setText("&Up");
        upButton.addListener(SWT.Selection, event -> handleUpButton(event));
        setButtonLayoutData(upButton);
        ((GridData) upButton.getLayoutData()).verticalIndent = tableLabelSize.y;
        upButton.setEnabled(false);

        downButton = new Button(bttArea, SWT.PUSH);
        downButton.setText("Dow&n");
        downButton.addListener(SWT.Selection, event -> handleDownButton(event));
        setButtonLayoutData(downButton);
        downButton.setEnabled(false);
        return bttArea;
    }

    private Control createWidthArea(Composite parent) {
        var dummy = new Label(parent, SWT.NONE);
        dummy.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));

        var widthComposite = new Composite(parent, SWT.NONE);
        var gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        widthComposite.setLayout(gridLayout);
        widthComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

        widthLabel = new Label(widthComposite, SWT.NONE);
        widthLabel.setText("&Width of the selected shown column:");
        var gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        widthLabel.setLayoutData(gridData);

        widthText = new Text(widthComposite, SWT.BORDER);
        widthText.addVerifyListener(e -> {
            if (e.character != 0 && e.keyCode != SWT.BS && e.keyCode != SWT.DEL && !Character.isDigit(e.character)) {
                e.doit = false;
            }
        });

        gridData = new GridData();
        gridData.widthHint = convertWidthInCharsToPixels(5);
        widthText.setLayoutData(gridData);
        widthText.addModifyListener(e -> {
            try {
                var width = Integer.parseInt(widthText.getText());
                var data = (ColumnDef) visibleViewer.getStructuredSelection().getFirstElement();
                if (data != null) {
                    data.width = width;
                }
            } catch (NumberFormatException ex) {
                // ignore
            }
        });
        widthLabel.setEnabled(false);
        widthText.setEnabled(false);
        return widthText;
    }

    private Control createVisibleTable(Composite parent) {
        var composite = new Composite(parent, SWT.NONE);
        var gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        var label = new Label(composite, SWT.NONE);
        label.setText("Sh&own:");

        var table = new Table(composite, SWT.BORDER | SWT.MULTI);
        var data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = convertWidthInCharsToPixels(20);
        data.heightHint = table.getItemHeight() * 15;
        table.setLayoutData(data);

        var column = new TableColumn(table, SWT.NONE);
        column.setText("Sh&own:");
        Listener columnResize = event -> column.setWidth(table.getClientArea().width);
        table.addListener(SWT.Resize, columnResize);

        visibleViewer = new TableViewer(table);
        visibleViewer.setLabelProvider(new TableLabelProvider());
        visibleViewer.setContentProvider(ArrayContentProvider.getInstance());
        visibleViewer.addSelectionChangedListener(event -> handleVisibleSelection(event.getSelection()));
        table.addListener(SWT.MouseDoubleClick, event -> handleToNonVisibleButton(event));
        visibleViewer.setInput(visible);
        return table;
    }

    private Control createInvisibleTable(Composite parent) {
        var composite = new Composite(parent, SWT.NONE);
        var gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);

        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        var label = new Label(composite, SWT.NONE);
        label.setText("H&idden:");
        applyDialogFont(label);
        tableLabelSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        var table = new Table(composite, SWT.BORDER | SWT.MULTI);
        var data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = convertWidthInCharsToPixels(20);
        data.heightHint = table.getItemHeight() * 15;
        table.setLayoutData(data);

        var column = new TableColumn(table, SWT.NONE);
        column.setText("H&idden:");
        Listener columnResize = event -> column.setWidth(table.getClientArea().width);
        table.addListener(SWT.Resize, columnResize);

        nonVisibleViewer = new TableViewer(table);
        nonVisibleViewer.setLabelProvider(new TableLabelProvider());
        nonVisibleViewer.setContentProvider(ArrayContentProvider.getInstance());
        nonVisibleViewer.addSelectionChangedListener(event -> handleNonVisibleSelection(event.getSelection()));
        table.addListener(SWT.MouseDoubleClick, event -> handleToVisibleButton(event));
        nonVisibleViewer.setInput(nonVisible);
        return table;
    }

    private Control createMoveButtons(Composite parent) {
        var composite = new Composite(parent, SWT.NONE);
        var compositeLayout = new GridLayout();
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        composite.setLayout(compositeLayout);
        composite.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));

        var bttArea = new Composite(composite, SWT.NONE);
        var layout = new GridLayout();
        layout.marginHeight = 0;
        bttArea.setLayout(layout);
        bttArea.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, true));

        toVisibleButton = new Button(bttArea, SWT.PUSH);
        toVisibleButton.setText("&Show ->");
        setButtonLayoutData(toVisibleButton);
        ((GridData) toVisibleButton.getLayoutData()).verticalIndent = tableLabelSize.y;
        toVisibleButton.addListener(SWT.Selection, event -> handleToVisibleButton(event));
        toVisibleButton.setEnabled(false);

        toNonVisibleButton = new Button(bttArea, SWT.PUSH);
        toNonVisibleButton.setText("<- &Hide");
        setButtonLayoutData(toNonVisibleButton);

        toNonVisibleButton.addListener(SWT.Selection, event -> handleToNonVisibleButton(event));
        toNonVisibleButton.setEnabled(false);

        return bttArea;
    }

    /**
     * Handles a selection change in the viewer that lists out the non-visible columns
     */
    void handleNonVisibleSelection(ISelection selection) {
        var nvKeys = ((IStructuredSelection) selection).toArray();
        toVisibleButton.setEnabled(nvKeys.length > 0);
        if (visibleViewer.getControl().isFocusControl() && visible.size() <= 1) {
            handleStatusUdpate(IStatus.INFO, "There must be at least one visible column.");
        } else {
            handleStatusUdpate(IStatus.INFO, getDefaultMessage());
        }
    }

    /**
     * Handles a selection change in the viewer that lists out the visible columns. Takes care of various enablements.
     */
    void handleVisibleSelection(ISelection selection) {
        @SuppressWarnings("unchecked")
        List<ColumnDef> selVCols = ((IStructuredSelection) selection).toList();
        var allVCols = visible;
        toNonVisibleButton.setEnabled(selVCols.size() > 0 && allVCols.size() > selVCols.size());

        boolean moveDown = !selVCols.isEmpty(), moveUp = !selVCols.isEmpty();
        var iterator = selVCols.iterator();
        while (iterator.hasNext()) {
            var columnObj = iterator.next();
            if (!columnObj.moveable) {
                moveUp = false;
                moveDown = false;
                break;
            }
            var i = allVCols.indexOf(columnObj);
            if (i == 0) {
                moveUp = false;
                if (!moveDown) {
                    break;
                }
            }
            if (i == (allVCols.size() - 1)) {
                moveDown = false;
                if (!moveUp) {
                    break;
                }
            }
        }
        upButton.setEnabled(moveUp);
        downButton.setEnabled(moveDown);

        var edit = selVCols.size() == 1 && selVCols.get(0).resizable;
        widthLabel.setEnabled(edit);
        widthText.setEnabled(edit);
        if (edit) {
            var width = selVCols.get(0).width;
            widthText.setText(Integer.toString(width));
        } else {
            widthText.setText("");
        }
    }

    /**
     * Applies to visible columns, and handles the changes in the order of columns
     */
    private void handleDownButton(Event e) {
        var selection = visibleViewer.getStructuredSelection();
        @SuppressWarnings("unchecked")
        List<ColumnDef> selVCols = selection.toList();
        var allVCols = visible;
        for (var i = selVCols.size() - 1; i >= 0; i--) {
            var colObj = selVCols.get(i);
            var index = allVCols.indexOf(colObj);
            colObj.newIndex = index + 1;
            allVCols.remove(index);
            allVCols.add(index + 1, colObj);
        }
        visibleViewer.refresh();
        handleVisibleSelection(selection);
    }

    /**
     * Applies to visible columns, and handles the changes in the order of columns
     */
    private void handleUpButton(Event e) {
        var selection = visibleViewer.getStructuredSelection();
        @SuppressWarnings("unchecked")
        List<ColumnDef> selVCols = selection.toList();
        var allVCols = visible;
        for (var i = 0; i < selVCols.size(); i++) {
            var colObj = selVCols.get(i);
            var index = allVCols.indexOf(colObj);
            colObj.newIndex = index - 1;
            allVCols.remove(index);
            allVCols.add(index - 1, colObj);
        }
        visibleViewer.refresh();
        handleVisibleSelection(selection);
    }

    /**
     * Moves selected columns from non-visible to visible state
     */
    private void handleToVisibleButton(Event e) {
        var selection = nonVisibleViewer.getStructuredSelection();
        @SuppressWarnings("unchecked")
        List<ColumnDef> selVCols = selection.toList();
        nonVisible.removeAll(selVCols);
        visible.addAll(selVCols);

        selVCols.forEach(c -> c.visible = true);
        renumber(visible);
        renumber(nonVisible);

        visibleViewer.refresh();
        visibleViewer.setSelection(selection);
        nonVisibleViewer.refresh();
        handleVisibleSelection(selection);
        handleNonVisibleSelection(nonVisibleViewer.getStructuredSelection());
    }

    /**
     * Moves selected columns from visible to non-visible state
     */
    protected void handleToNonVisibleButton(Event e) {
        if (visibleViewer.getControl().isFocusControl() && visible.size() <= 1) {
            handleStatusUdpate(IStatus.INFO, "There must be at least one visible column.");
            return;
        }
        var structuredSelection = visibleViewer.getStructuredSelection();
        @SuppressWarnings("unchecked")
        List<ColumnDef> selVCols = structuredSelection.toList();
        visible.removeAll(selVCols);
        nonVisible.addAll(selVCols);

        selVCols.forEach(c -> c.visible = false);
        renumber(visible);
        renumber(nonVisible);

        nonVisibleViewer.refresh();
        nonVisibleViewer.setSelection(structuredSelection);
        visibleViewer.refresh();
        handleVisibleSelection(structuredSelection);
        handleNonVisibleSelection(structuredSelection);
    }

    private void renumber(List<ColumnDef> list) {
        var iterator = list.listIterator();
        while (iterator.hasNext()) {
            iterator.next().newIndex = iterator.previousIndex();
        }
    }

    public List<ColumnDef> getVisible() {
        return visible;
    }

    public List<ColumnDef> getNonVisible() {
        return nonVisible;
    }

    @Override
    protected void performDefaults() {
        if (nonVisibleViewer != null) {
            nonVisibleViewer.refresh();
        }
        if (visibleViewer != null) {
            visibleViewer.refresh();
        }
        super.performDefaults();
    }

    public static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            var text = getText(element);
            if (text == null || text.equals("")) {
                return "(no-name)";
            } else {
                return text;
            }
        }
    }
}
