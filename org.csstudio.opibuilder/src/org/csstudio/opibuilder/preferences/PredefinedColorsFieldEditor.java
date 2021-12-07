/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PredefinedColorsFieldEditor extends FieldEditor {

    private TableViewer tableViewer;

    private Composite buttonBox;
    private Button addButton;
    private Button editButton;
    private Button removeButton;

    private ResourceManager resourceManager;

    private Map<RGB, Color> colorCache = new HashMap<>();

    public PredefinedColorsFieldEditor(String name, Composite parent) {
        super(name, "Predefined Colors:", parent);
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control control = getLabelControl(parent);
        GridDataFactory.swtDefaults().span(numColumns, 1).applyTo(control);

        createTableControl(parent);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(numColumns - 1, 1).grab(true, true)
                .applyTo(tableViewer.getTable());

        buttonBox = getButtonControl(parent);
        updateButtonStatus();
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
    }

    private void createTableControl(Composite parent) {
        var tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        var tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);

        tableViewer = new TableViewer(tableWrapper, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());

        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                var c1 = (NamedColor) e1;
                var c2 = (NamedColor) e2;
                return c1.name.compareToIgnoreCase(c2.name);
            }
        });

        var colorColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        colorColumn.setLabelProvider(new ColorLabelProvider() {
            @Override
            public Color getColor(Object element) {
                var color = (NamedColor) element;
                return colorCache.computeIfAbsent(color.rgb, resourceManager::createColor);
            }

            @Override
            public Color getBorderColor(Object element) {
                return parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
            }
        });
        tcl.setColumnData(colorColumn.getColumn(), new ColumnWeightData(50, 50, false));

        var textColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        textColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var color = (NamedColor) element;
                return color.name;
            }
        });
        tcl.setColumnData(textColumn.getColumn(), new ColumnWeightData(200, 200, true));

        tableViewer.getTable().addListener(SWT.Selection, evt -> updateButtonStatus());

        tableViewer.getTable().addListener(SWT.MouseDoubleClick, e -> {
            var item = tableViewer.getTable().getItem(new Point(e.x, e.y));
            if (item == null) {
                return;
            }

            var bounds = item.getBounds();
            var isClickOnCheckbox = e.x < bounds.x;
            if (isClickOnCheckbox) {
                return;
            }

            var selectedRule = getSelectedColor();
            editColor(selectedRule);
            updateButtonStatus();
        });
    }

    private Composite getButtonControl(Composite parent) {
        var box = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(box);

        addButton = createButton(box, "Add...");
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addNewColor();
                updateButtonStatus();
            }
        });

        editButton = createButton(box, "Edit...");
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editColor(getSelectedColor());
                updateButtonStatus();
            }
        });

        editButton.setEnabled(false);

        removeButton = createButton(box, "Remove");
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeRule(getSelectedColor());
                updateButtonStatus();
            }
        });

        return box;
    }

    private NamedColor getSelectedColor() {
        var tableInput = getTableViewerInput();
        if (tableInput == null) {
            return null;
        }

        var index = tableViewer.getTable().getSelectionIndex();
        if (index < 0) {
            return null;
        }
        return tableInput.get(index);
    }

    @SuppressWarnings("unchecked")
    private List<NamedColor> getTableViewerInput() {
        return (List<NamedColor>) tableViewer.getInput();
    }

    private void updateButtonStatus() {
        var selectedRule = getSelectedColor();
        if (selectedRule == null) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            return;
        }
        editButton.setEnabled(true);
        removeButton.setEnabled(true);
    }

    private void removeRule(NamedColor selectedRule) {
        var list = getTableViewerInput();
        list.remove(selectedRule);
        tableViewer.refresh();
    }

    private void addNewColor() {
        List<String> existingNames = getTableViewerInput().stream().map(c -> c.name).collect(Collectors.toList());

        var shell = tableViewer.getTable().getShell();
        var dialog = new NamedColorDialog(shell, null, existingNames);
        if (dialog.open() == Window.OK) {
            var newColor = dialog.getColor();
            var list = getTableViewerInput();
            list.add(newColor);
            tableViewer.refresh();
        }
    }

    private void editColor(NamedColor selectedColor) {
        List<String> existingNames = getTableViewerInput().stream().map(c -> c.name).collect(Collectors.toList());

        var shell = tableViewer.getTable().getShell();
        var dialog = new NamedColorDialog(shell, selectedColor, existingNames);
        if (dialog.open() == Window.OK) {
            var updatedColor = dialog.getColor();
            var list = getTableViewerInput();
            var indexOfOriginalColor = list.indexOf(selectedColor);
            list.remove(indexOfOriginalColor);
            list.add(indexOfOriginalColor, updatedColor);
            tableViewer.refresh();
        }
    }

    private Button createButton(Composite box, String text) {
        var button = new Button(box, SWT.PUSH);
        button.setText(text);

        var widthHint = Math.max(convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH),
                button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(widthHint, SWT.DEFAULT).applyTo(button);

        return button;
    }

    @Override
    protected void doLoad() {
        var rules = OPIBuilderPlugin.getDefault().loadColors();
        tableViewer.setInput(rules);
    }

    @Override
    protected void doLoadDefault() {
        var rules = OPIBuilderPlugin.getDefault().loadDefaultColors();
        tableViewer.setInput(rules);
    }

    @Override
    protected void doStore() {
        var rules = getTableViewerInput();
        OPIBuilderPlugin.getDefault().storeColors(rules);
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }
}
