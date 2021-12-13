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

import java.util.List;
import java.util.stream.Collectors;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.util.OPIFont;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.StringConverter;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class PredefinedFontsFieldEditor extends FieldEditor {

    private TableViewer tableViewer;

    private Composite buttonBox;
    private Button addButton;
    private Button editButton;
    private Button removeButton;

    private Label preview;

    public PredefinedFontsFieldEditor(String name, Composite parent) {
        super(name, "Predefined Fonts:", parent);
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

        var previewGroup = new Group(parent, SWT.NONE);
        previewGroup.setText("Preview:");
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 70;
        previewGroup.setLayoutData(gd);
        var gl = new GridLayout();
        previewGroup.setLayout(gl);
        preview = new Label(previewGroup, SWT.NONE);
        preview.setText("The quick brown fox jumps over the lazy dog");
        gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        gd.verticalAlignment = SWT.CENTER;
        preview.setLayoutData(gd);

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
        tableViewer.getTable().setHeaderVisible(true);

        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                var f1 = (OPIFont) e1;
                var f2 = (OPIFont) e2;
                return f1.getFontMacroName().compareToIgnoreCase(f2.getFontMacroName());
            }
        });

        var textColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        textColumn.getColumn().setText("Name");
        textColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var font = (OPIFont) element;
                return font.getFontMacroName();
            }
        });
        tcl.setColumnData(textColumn.getColumn(), new ColumnWeightData(100, true));

        var descColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        descColumn.getColumn().setText("Font");
        descColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var font = (OPIFont) element;
                return StringConverter.asString(font.getRawFontData());
            }
        });
        tcl.setColumnData(descColumn.getColumn(), new ColumnWeightData(200, true));

        tableViewer.getTable().addListener(SWT.Selection, evt -> {
            updatePreviewFont();
            updateButtonStatus();
        });

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

            var selectedFont = getSelectedFont();
            editFont(selectedFont);
            updatePreviewFont();
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
                addNewFont();
                updateButtonStatus();
            }
        });

        editButton = createButton(box, "Edit...");
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editFont(getSelectedFont());
                updateButtonStatus();
            }
        });

        editButton.setEnabled(false);

        removeButton = createButton(box, "Remove");
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeFont(getSelectedFont());
                updateButtonStatus();
            }
        });

        return box;
    }

    private OPIFont getSelectedFont() {
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
    private List<OPIFont> getTableViewerInput() {
        return (List<OPIFont>) tableViewer.getInput();
    }

    private void updateButtonStatus() {
        var selectedFont = getSelectedFont();
        if (selectedFont == null) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            return;
        }
        editButton.setEnabled(true);
        removeButton.setEnabled(true);
    }

    private void removeFont(OPIFont selectedFont) {
        var list = getTableViewerInput();
        list.remove(selectedFont);
        tableViewer.refresh();
    }

    private void addNewFont() {
        List<String> existingNames = getTableViewerInput().stream().map(OPIFont::getFontName)
                .collect(Collectors.toList());

        var shell = tableViewer.getTable().getShell();
        var dialog = new OPIFontDialog(shell, null, existingNames);
        if (dialog.open() == Window.OK) {
            var newFont = dialog.getFont();
            var list = getTableViewerInput();
            list.add(newFont);
            tableViewer.refresh();
            updatePreviewFont();
        }
    }

    private void editFont(OPIFont selectedFont) {
        List<String> existingNames = getTableViewerInput().stream().map(OPIFont::getFontName)
                .collect(Collectors.toList());

        var shell = tableViewer.getTable().getShell();
        var dialog = new OPIFontDialog(shell, selectedFont, existingNames);
        if (dialog.open() == Window.OK) {
            var updatedFont = dialog.getFont();
            var list = getTableViewerInput();
            var indexOfOriginalFont = list.indexOf(selectedFont);
            list.remove(indexOfOriginalFont);
            list.add(indexOfOriginalFont, updatedFont);
            tableViewer.refresh();
            updatePreviewFont();
        }
    }

    private void updatePreviewFont() {
        var selectedFont = getSelectedFont();
        if (preview != null && selectedFont != null) {
            preview.setFont(selectedFont.getSWTFont());
            preview.getParent().layout();
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
        var fonts = OPIBuilderPlugin.getDefault().loadFonts();
        tableViewer.setInput(fonts);
    }

    @Override
    protected void doLoadDefault() {
        var fonts = OPIBuilderPlugin.getDefault().loadDefaultFonts();
        tableViewer.setInput(fonts);
    }

    @Override
    protected void doStore() {
        var fonts = getTableViewerInput();
        OPIBuilderPlugin.getDefault().storeFonts(fonts);
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }
}
