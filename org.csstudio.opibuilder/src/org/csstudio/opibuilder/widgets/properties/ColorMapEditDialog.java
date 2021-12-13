/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.properties;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.visualparts.RGBColorCellEditor;
import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorTuple;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class ColorMapEditDialog extends TrayDialog {

    private Action addAction;
    private Action copyAction;
    private Action removeAction;
    private Action moveUpAction;
    private Action moveDownAction;

    private TableViewer colorListViewer;

    private List<ColorTuple> colorList;
    private PredefinedColorMap predefinedColorMap;
    private boolean autoScale;
    private boolean interpolate;

    private String title;
    private Combo preDefinedMapCombo;
    private double[] mapData;
    private Label colorMapLabel;
    private Image colorMapImage;
    private double min, max;

    public ColorMapEditDialog(Shell parentShell, ColorMap colorMap, String dialogTitle, double min, double max) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        colorList = new LinkedList<>();
        for (var value : colorMap.getMap().keySet()) {
            colorList.add(new ColorTuple(value, colorMap.getMap().get(value)));
        }
        autoScale = colorMap.isAutoScale();
        interpolate = colorMap.isInterpolate();
        predefinedColorMap = colorMap.getPredefinedColorMap();
        title = dialogTitle;
        this.min = min;
        this.max = max;
        mapData = new double[256];
        for (var j = 0; j < 256; j++) {
            mapData[j] = min + j * (max - min) / 255.0;
        }
    }

    public ColorMap getOutput() {
        var result = new ColorMap();
        if (predefinedColorMap == PredefinedColorMap.None) {
            var map = new LinkedHashMap<Double, RGB>();
            for (var tuple : colorList) {
                map.put(tuple.value, tuple.rgb);
            }
            result.setColorMap(map);
        }
        result.setAutoScale(autoScale);
        result.setInterpolate(interpolate);
        result.setPredefinedColorMap(predefinedColorMap);
        return result;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    private void createLabel(Composite parent, String text) {
        var label = new Label(parent, SWT.WRAP);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 2, 1));
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
        var grid = new GridData();
        grid.horizontalAlignment = GridData.FILL;
        grid.verticalAlignment = GridData.BEGINNING;
        toolBar.setLayoutData(grid);
        createActions();
        toolbarManager.add(addAction);
        toolbarManager.add(copyAction);
        toolbarManager.add(removeAction);
        toolbarManager.add(moveUpAction);
        toolbarManager.add(moveDownAction);

        toolbarManager.update(true);

        var mainComposite = new Composite(topComposite, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        mainComposite.setLayout(gl);
        var gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 300;
        mainComposite.setLayoutData(gridData);

        var leftComposite = new Composite(mainComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        leftComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 250;
        leftComposite.setLayoutData(gd);

        colorListViewer = createColorListViewer(leftComposite);
        colorListViewer.setInput(colorList);

        var rightComposite = new Composite(mainComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        rightComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 340;
        rightComposite.setLayoutData(gd);
        createLabel(rightComposite, "Use predefined color map:");

        preDefinedMapCombo = new Combo(rightComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        preDefinedMapCombo.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
        preDefinedMapCombo.setItems(PredefinedColorMap.getStringValues());

        var i = 0;
        for (var colorMap : PredefinedColorMap.values()) {
            if (predefinedColorMap == colorMap) {
                break;
            } else {
                i++;
            }
        }
        preDefinedMapCombo.select(i);

        var interpolateCheckBox = new Button(rightComposite, SWT.CHECK);
        interpolateCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
        interpolateCheckBox.setSelection(interpolate);
        interpolateCheckBox.setText("Interpolate");

        var autoScaleCheckBox = new Button(rightComposite, SWT.CHECK);
        autoScaleCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
        autoScaleCheckBox.setSelection(autoScale);
        autoScaleCheckBox.setText("Auto Scale");
        autoScaleCheckBox.setToolTipText("Scale the color map values to the range of" + " (" + min + ", " + max + ").");

        var group = new Group(rightComposite, SWT.None);
        group.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));
        group.setLayout(new GridLayout(2, false));
        group.setText("Output" + " (" + min + "~" + max + ")");

        colorMapLabel = new Label(group, SWT.None);
        colorMapLabel.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));

        refreshGUI();

        preDefinedMapCombo.addModifyListener(e -> {
            predefinedColorMap = PredefinedColorMap.values()[preDefinedMapCombo.getSelectionIndex()];
            if (preDefinedMapCombo.getSelectionIndex() != 0) {
                var map = PredefinedColorMap.values()[preDefinedMapCombo.getSelectionIndex()].getMap();
                colorList.clear();
                for (var entry : map.entrySet()) {
                    colorList.add(new ColorTuple(entry.getKey(), entry.getValue()));
                }
                colorListViewer.refresh();
            }
            refreshGUI();
        });

        interpolateCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                interpolate = interpolateCheckBox.getSelection();
                refreshGUI();
            }
        });

        autoScaleCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                autoScale = autoScaleCheckBox.getSelection();
                refreshGUI();
            }
        });

        return parentComposite;
    }

    /**
     * Refresh GUI when color map data changed.
     */
    private void refreshGUI() {
        var originalImage = new Image(Display.getCurrent(), getOutput().drawImage(mapData, 256, 1, max, min));

        if (colorMapImage != null && !colorMapImage.isDisposed()) {
            colorMapImage.dispose();
            colorMapImage = null;
        }
        colorMapImage = new Image(Display.getCurrent(), 300, 40);
        var gc = new GC(colorMapImage);
        gc.drawImage(originalImage, 0, 0, 256, 1, 0, 0, colorMapImage.getBounds().width,
                colorMapImage.getBounds().height);
        colorMapLabel.setImage(colorMapImage);
        colorMapLabel.setAlignment(SWT.CENTER);
        gc.dispose();
        originalImage.dispose();
    }

    @Override
    public boolean close() {
        if (colorMapImage != null && !colorMapImage.isDisposed()) {
            colorMapImage.dispose();
            colorMapImage = null;
        }
        return super.close();
    }

    /**
     * Refreshes the enabled-state of the actions.
     */
    private void refreshToolbarOnSelection() {

        var selection = (IStructuredSelection) colorListViewer.getSelection();
        if (!selection.isEmpty() && selection.getFirstElement() instanceof ColorTuple) {
            removeAction.setEnabled(true);
            moveUpAction.setEnabled(true);
            moveDownAction.setEnabled(true);
            copyAction.setEnabled(true);
        } else {
            removeAction.setEnabled(false);
            moveUpAction.setEnabled(false);
            moveDownAction.setEnabled(false);
            copyAction.setEnabled(false);
        }
    }

    /**
     * @param tuple
     *            the tuple to be selected
     */
    private void refreshColorListViewerForAction(ColorTuple tuple) {
        colorListViewer.refresh();
        if (tuple == null) {
            colorListViewer.setSelection(StructuredSelection.EMPTY);
        } else {
            colorListViewer.setSelection(new StructuredSelection(tuple));
        }
        preDefinedMapCombo.select(0);
        refreshGUI();
    }

    private TableViewer createColorListViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        var tvColumn = new TableViewerColumn(viewer, SWT.NONE);
        tvColumn.getColumn().setText("Value");
        tvColumn.getColumn().setMoveable(false);
        tvColumn.getColumn().setWidth(100);
        tvColumn.setEditingSupport(new ValueColumnEditingSupport(viewer, viewer.getTable()));

        tvColumn.getColumn().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                var table = viewer.getTable();
                var dir = table.getSortDirection();
                dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                viewer.getTable().setSortDirection(dir);
                var colorTupleArray = colorList.toArray();
                Arrays.sort(colorTupleArray);
                colorList.clear();
                var i = 0;
                var array = new ColorTuple[colorTupleArray.length];
                for (var o : colorTupleArray) {
                    if (dir == SWT.UP) {
                        array[i++] = (ColorTuple) o;
                    } else {
                        array[colorTupleArray.length - 1 - i++] = (ColorTuple) o;
                    }
                }
                colorList.addAll(Arrays.asList(array));
                viewer.refresh();
            }
        });

        var colorColumn = new TableViewerColumn(viewer, SWT.NONE);
        colorColumn.getColumn().setText("Color");
        colorColumn.getColumn().setMoveable(false);
        colorColumn.getColumn().setWidth(100);
        colorColumn.setEditingSupport(new ColorColumnEditingSupport(viewer, viewer.getTable()));

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new ColorListLabelProvider());
        viewer.getTable().setSortColumn(tvColumn.getColumn());
        viewer.getTable().setSortDirection(SWT.UP);

        viewer.addSelectionChangedListener(event -> refreshToolbarOnSelection());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return viewer;
    }

    private void createActions() {
        addAction = new Action("Add") {
            @Override
            public void run() {
                var tuple = new ColorTuple(0, new RGB(0, 0, 0));
                colorList.add(tuple);
                refreshColorListViewerForAction(tuple);
            }
        };
        addAction.setToolTipText("Add a color tuple");
        addAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/add.gif"));

        copyAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) colorListViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ColorTuple) {
                    var o = (ColorTuple) selection.getFirstElement();
                    var tuple = new ColorTuple(o.value, o.rgb);
                    colorList.add(tuple);
                    refreshColorListViewerForAction(tuple);
                }
            }
        };
        copyAction.setText("Copy");
        copyAction.setToolTipText("Copy the selected color tuple");
        copyAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/copy.gif"));
        copyAction.setEnabled(false);

        removeAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) colorListViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ColorTuple) {
                    colorList.remove(selection.getFirstElement());
                    refreshColorListViewerForAction(null);
                    setEnabled(false);
                }
            }
        };
        removeAction.setText("Remove");
        removeAction.setToolTipText("Remove the selected color tuple from the list");
        removeAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/delete.gif"));
        removeAction.setEnabled(false);

        moveUpAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) colorListViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ColorTuple) {
                    var tuple = (ColorTuple) selection.getFirstElement();
                    var i = colorList.indexOf(tuple);
                    if (i > 0) {
                        colorList.remove(tuple);
                        colorList.add(i - 1, tuple);
                        refreshColorListViewerForAction(tuple);
                    }
                }
            }
        };
        moveUpAction.setText("Move Up");
        moveUpAction.setToolTipText("Move up the selected color tuple");
        moveUpAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_prev.gif"));
        moveUpAction.setEnabled(false);

        moveDownAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) colorListViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ColorTuple) {
                    var tuple = (ColorTuple) selection.getFirstElement();
                    var i = colorList.indexOf(tuple);
                    if (i < colorList.size() - 1) {
                        colorList.remove(tuple);
                        colorList.add(i + 1, tuple);
                        refreshColorListViewerForAction(tuple);
                    }
                }
            }
        };
        moveDownAction.setText("Move Down");
        moveDownAction.setToolTipText("Move down the selected color tuple");
        moveDownAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_next.gif"));
        moveDownAction.setEnabled(false);
    }

    private final static class ColorListLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 1 && element instanceof ColorTuple) {
                return new OPIColor(((ColorTuple) element).rgb).getImage();
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0 && element instanceof ColorTuple) {
                return Double.toString(((ColorTuple) element).value);
            }
            if (columnIndex == 1 && element instanceof ColorTuple) {
                return new OPIColor(((ColorTuple) element).rgb).toString();
            }
            return null;
        }
    }

    private final class ValueColumnEditingSupport extends EditingSupport {

        private Table table;

        public ValueColumnEditingSupport(ColumnViewer viewer, Table table) {
            super(viewer);
            this.table = table;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new TextCellEditor(table);
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof ColorTuple) {
                return Double.toString(((ColorTuple) element).value);
            }
            return null;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof ColorTuple) {
                var s = value == null ? "0" : value.toString();
                try {
                    ((ColorTuple) element).value = Double.parseDouble(s);
                    getViewer().refresh();
                    preDefinedMapCombo.select(0);
                    refreshGUI();
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    private final class ColorColumnEditingSupport extends EditingSupport {

        private Table table;

        public ColorColumnEditingSupport(ColumnViewer viewer, Table table) {
            super(viewer);
            this.table = table;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new RGBColorCellEditor(table);
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof ColorTuple) {
                return ((ColorTuple) element).rgb;
            }
            return null;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof ColorTuple) {
                ((ColorTuple) element).rgb = (RGB) value;
                getViewer().refresh();
                preDefinedMapCombo.select(0);
                refreshGUI();
            }
        }
    }
}
