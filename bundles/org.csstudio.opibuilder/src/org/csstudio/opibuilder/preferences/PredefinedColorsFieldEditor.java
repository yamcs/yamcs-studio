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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

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
        Composite tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);

        tableViewer = new TableViewer(tableWrapper, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());

        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                NamedColor c1 = (NamedColor) e1;
                NamedColor c2 = (NamedColor) e2;
                return c1.name.compareToIgnoreCase(c2.name);
            }
        });

        TableViewerColumn colorColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        colorColumn.setLabelProvider(new ColorLabelProvider() {
            @Override
            public Color getColor(Object element) {
                NamedColor color = (NamedColor) element;
                return colorCache.computeIfAbsent(color.rgb, resourceManager::createColor);
            }

            @Override
            public Color getBorderColor(Object element) {
                return parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
            }
        });
        tcl.setColumnData(colorColumn.getColumn(), new ColumnWeightData(50, 50, false));

        TableViewerColumn textColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        textColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                NamedColor color = (NamedColor) element;
                return color.name;
            }
        });
        tcl.setColumnData(textColumn.getColumn(), new ColumnWeightData(200, 200, true));

        tableViewer.getTable().addListener(SWT.Selection, evt -> updateButtonStatus());

        tableViewer.getTable().addListener(SWT.MouseDoubleClick, e -> {
            TableItem item = tableViewer.getTable().getItem(new Point(e.x, e.y));
            if (item == null) {
                return;
            }

            Rectangle bounds = item.getBounds();
            boolean isClickOnCheckbox = e.x < bounds.x;
            if (isClickOnCheckbox) {
                return;
            }

            NamedColor selectedRule = getSelectedColor();
            editColor(selectedRule);
            updateButtonStatus();
        });
    }

    private Composite getButtonControl(Composite parent) {
        Composite box = new Composite(parent, SWT.NONE);
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
        List<NamedColor> tableInput = getTableViewerInput();
        if (tableInput == null) {
            return null;
        }

        int index = tableViewer.getTable().getSelectionIndex();
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
        NamedColor selectedRule = getSelectedColor();
        if (selectedRule == null) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            return;
        }
        editButton.setEnabled(true);
        removeButton.setEnabled(true);
    }

    private void removeRule(NamedColor selectedRule) {
        List<NamedColor> list = getTableViewerInput();
        list.remove(selectedRule);
        tableViewer.refresh();
    }

    private void addNewColor() {
        List<String> existingNames = getTableViewerInput().stream()
                .map(c -> c.name)
                .collect(Collectors.toList());

        Shell shell = tableViewer.getTable().getShell();
        NamedColorDialog dialog = new NamedColorDialog(shell, null, existingNames);
        if (dialog.open() == Window.OK) {
            NamedColor newColor = dialog.getColor();
            List<NamedColor> list = getTableViewerInput();
            list.add(newColor);
            tableViewer.refresh();
        }
    }

    private void editColor(NamedColor selectedColor) {
        List<String> existingNames = getTableViewerInput().stream()
                .map(c -> c.name)
                .collect(Collectors.toList());

        Shell shell = tableViewer.getTable().getShell();
        NamedColorDialog dialog = new NamedColorDialog(shell, selectedColor, existingNames);
        if (dialog.open() == Window.OK) {
            NamedColor updatedColor = dialog.getColor();
            List<NamedColor> list = getTableViewerInput();
            int indexOfOriginalColor = list.indexOf(selectedColor);
            list.remove(indexOfOriginalColor);
            list.add(indexOfOriginalColor, updatedColor);
            tableViewer.refresh();
        }
    }

    private Button createButton(Composite box, String text) {
        Button button = new Button(box, SWT.PUSH);
        button.setText(text);

        int widthHint = Math.max(convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH),
                button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(widthHint, SWT.DEFAULT).applyTo(button);

        return button;
    }

    @Override
    protected void doLoad() {
        List<NamedColor> rules = OPIBuilderPlugin.getDefault().loadColors();
        tableViewer.setInput(rules);
    }

    @Override
    protected void doLoadDefault() {
        List<NamedColor> rules = OPIBuilderPlugin.getDefault().loadDefaultColors();
        tableViewer.setInput(rules);
    }

    @Override
    protected void doStore() {
        List<NamedColor> rules = getTableViewerInput();
        OPIBuilderPlugin.getDefault().storeColors(rules);
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }
}
