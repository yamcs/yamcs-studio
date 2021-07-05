package org.yamcs.studio.eventlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

public class ColoringRulesFieldEditor extends FieldEditor {

    private TableViewer tableViewer;

    private Composite buttonBox;
    private Button addButton;
    private Button editButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;

    private ResourceManager resourceManager;

    private Map<RGB, Color> colorCache = new HashMap<>();

    public ColoringRulesFieldEditor(String name, Composite parent) {
        super(name, "Coloring Rules:", parent);
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control control = getLabelControl(parent);
        GridDataFactory.swtDefaults().span(numColumns, 1).applyTo(control);

        tableViewer = getTableControl(parent);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(numColumns - 1, 1).grab(true, true)
                .applyTo(tableViewer.getTable());

        buttonBox = getButtonControl(parent);
        updateButtonStatus();
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
    }

    private TableViewer getTableControl(Composite parent) {
        TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());

        tableViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ColoringRule) element).expression;
            }

            @Override
            public Color getBackground(Object element) {
                ColoringRule rule = (ColoringRule) element;
                return colorCache.computeIfAbsent(rule.bg, resourceManager::createColor);
            }

            @Override
            public Color getForeground(Object element) {
                ColoringRule rule = (ColoringRule) element;
                return colorCache.computeIfAbsent(rule.fg, resourceManager::createColor);
            }
        });

        tableViewer.getTable().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonStatus();
            }
        });

        tableViewer.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TableItem item = tableViewer.getTable().getItem(new Point(e.x, e.y));
                if (item == null) {
                    return;
                }

                Rectangle bounds = item.getBounds();
                boolean isClickOnCheckbox = e.x < bounds.x;
                if (isClickOnCheckbox) {
                    return;
                }

                ColoringRule selectedRule = getSelectedRule();
                editRule(selectedRule);
                updateButtonStatus();
            }
        });
        return tableViewer;
    }

    private Composite getButtonControl(Composite parent) {
        Composite box = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(box);

        addButton = createButton(box, "Add...");
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addNewRule();
                updateButtonStatus();
            }
        });

        editButton = createButton(box, "Edit...");
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editRule(getSelectedRule());
                updateButtonStatus();
            }
        });

        editButton.setEnabled(false);

        removeButton = createButton(box, "Remove");
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeRule(getSelectedRule());
                updateButtonStatus();
            }
        });

        upButton = createButton(box, "Up");
        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveRuleUp(getSelectedRule());
                updateButtonStatus();
            }
        });
        downButton = createButton(box, "Down");
        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveRuleDown(getSelectedRule());
                updateButtonStatus();
            }
        });

        return box;
    }

    private ColoringRule getSelectedRule() {
        List<ColoringRule> tableInput = getTableViewerInput();
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
    private List<ColoringRule> getTableViewerInput() {
        return (List<ColoringRule>) tableViewer.getInput();
    }

    private void updateButtonStatus() {
        int selectionIndex = tableViewer.getTable().getSelectionIndex();
        ColoringRule selectedRule = getSelectedRule();
        if (selectedRule == null) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            return;
        }
        editButton.setEnabled(true);
        removeButton.setEnabled(true);
        upButton.setEnabled(selectionIndex > 0);
        downButton.setEnabled(selectionIndex < tableViewer.getTable().getItemCount() - 1);
    }

    private void removeRule(ColoringRule selectedRule) {
        List<ColoringRule> list = getTableViewerInput();
        list.remove(selectedRule);
        tableViewer.refresh();
    }

    private void addNewRule() {
        Shell shell = tableViewer.getTable().getShell();
        ColoringRuleDialog newRuleDialog = new ColoringRuleDialog(shell, null);
        if (newRuleDialog.open() == Window.OK) {
            ColoringRule newRule = newRuleDialog.getRule();
            List<ColoringRule> list = getTableViewerInput();
            list.add(newRule);
            tableViewer.refresh();
        }
    }

    private void editRule(ColoringRule selectedRule) {
        Shell shell = tableViewer.getTable().getShell();
        ColoringRuleDialog editRuleDialog = new ColoringRuleDialog(shell, selectedRule);
        if (editRuleDialog.open() == Window.OK) {
            ColoringRule updatedRule = editRuleDialog.getRule();
            List<ColoringRule> list = getTableViewerInput();
            int indexOfOriginalRule = list.indexOf(selectedRule);
            list.remove(indexOfOriginalRule);
            list.add(indexOfOriginalRule, updatedRule);
            tableViewer.refresh();
        }
    }

    private void moveRuleUp(ColoringRule selectedRule) {
        List<ColoringRule> list = getTableViewerInput();
        int index = list.indexOf(selectedRule);
        list.remove(index);
        list.add(index - 1, selectedRule);
        tableViewer.refresh();
    }

    private void moveRuleDown(ColoringRule selectedRule) {
        List<ColoringRule> list = getTableViewerInput();
        int index = list.indexOf(selectedRule);
        list.remove(index);
        list.add(index + 1, selectedRule);
        tableViewer.refresh();
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
        List<ColoringRule> rules = EventLogPlugin.getDefault().loadColoringRules();
        tableViewer.setInput(rules);
    }

    @Override
    protected void doLoadDefault() {
        List<ColoringRule> rules = EventLogPlugin.getDefault().loadDefaultColoringRules();
        tableViewer.setInput(rules);
    }

    @Override
    protected void doStore() {
        List<ColoringRule> rules = getTableViewerInput();
        EventLogPlugin.getDefault().storeColoringRules(rules);
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }
}
