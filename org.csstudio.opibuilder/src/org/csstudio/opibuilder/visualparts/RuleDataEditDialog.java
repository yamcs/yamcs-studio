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

import java.util.Arrays;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.script.Expression;
import org.csstudio.opibuilder.script.RuleData;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The dialog for rule data editing.
 */
public class RuleDataEditDialog extends TrayDialog {

    private Action addAction;
    private Action copyAction;
    private Action removeAction;
    private Action moveUpAction;
    private Action moveDownAction;

    private TableViewer expressionViewer;
    private PVTupleTableEditor pvsEditor;

    private Text nameText;
    private Combo propCombo;
    private Button outPutExpButton;

    private RuleData ruleData;
    private List<Expression> expressionList;

    private List<String> propIDList;
    private TableViewerColumn valueColumn;

    private static String[] UNCHANGEABLE_PROPERTIES = new String[] { AbstractWidgetModel.PROP_ACTIONS,
            AbstractWidgetModel.PROP_WIDGET_TYPE, AbstractWidgetModel.PROP_SCRIPTS, AbstractWidgetModel.PROP_RULES,
            AbstractContainerModel.PROP_MACROS };

    public RuleDataEditDialog(Shell parentShell, RuleData ruleData) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.ruleData = ruleData.getCopy();
        this.expressionList = this.ruleData.getExpressionList();

        var propIDSet = ruleData.getWidgetModel().getAllPropertyIDs();

        for (var p : UNCHANGEABLE_PROPERTIES) {
            propIDSet.remove(p);
        }
        for (var id : propIDSet.toArray(new String[0])) {
            var prop = ruleData.getWidgetModel().getProperty(id);
            if (prop.configurableByRule()) {
                continue;
            } else {
                propIDSet.remove(id);
            }
        }
        var propArray = propIDSet.toArray(new String[0]);
        Arrays.sort(propArray);
        propIDList = Arrays.asList(propArray);
    }

    @Override
    protected void okPressed() {
        var hasTrigger = false;
        for (var pvTuple : ruleData.getPVList()) {
            hasTrigger |= pvTuple.trigger;
        }
        if (!hasTrigger) {
            MessageDialog.openWarning(getShell(), "Warning",
                    NLS.bind("At least one trigger PV must be selected for the rule:\n{0}", ruleData.getName()));
            return;
        }

        super.okPressed();
    }

    public final RuleData getOutput() {
        return ruleData;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);

        shell.setText("Edit Rule");
    }

    /**
     * Creates 'wrapping' label with the given text.
     *
     * @param parent
     *            The parent for the label
     * @param text
     *            The text for the label
     */
    private void createLabel(Composite parent, String text) {
        var label = new Label(parent, SWT.WRAP);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.FILL, 0, false, false));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parentComposite = (Composite) super.createDialogArea(parent);

        var mainComposite = new Composite(parentComposite, SWT.NONE);
        mainComposite.setLayout(new GridLayout(2, false));
        var gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 500;
        mainComposite.setLayoutData(gridData);

        var topComposite = new Composite(mainComposite, SWT.NONE);
        topComposite.setLayout(new GridLayout(2, false));
        var gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.horizontalSpan = 2;
        topComposite.setLayoutData(gd);
        createLabel(topComposite, "Rule Name: ");

        nameText = new Text(topComposite, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameText.setText(ruleData.getName());
        nameText.addModifyListener(e -> ruleData.setName(nameText.getText()));
        createLabel(topComposite, "Property: ");

        propCombo = new Combo(topComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        propCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        var comboItems = new String[propIDList.size()];
        var i = 0;
        for (var id : propIDList) {
            comboItems[i++] = ruleData.getWidgetModel().getProperty(id).getDescription() + " (" + id + ")";
        }
        propCombo.setItems(comboItems);
        propCombo.select(propIDList.indexOf(ruleData.getPropId()));
        propCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (propIDList.get(propCombo.getSelectionIndex()).equals(AbstractPVWidgetModel.PROP_PVVALUE)) {
                    MessageDialog.openWarning(propCombo.getShell(), "Warning",
                            "Note: Changing pv_value property with rule or " + "script will not write value to the PV. "
                                    + "It only change the graphical value on the widget! "
                                    + "If you need to write a PV, please call PV.setValue() from script.");
                }
                ruleData.setPropId(propIDList.get(propCombo.getSelectionIndex()));
                if (ruleData.getProperty().getPropertyDescriptor() == null
                        || ruleData.getProperty().onlyAcceptExpressionInRule()) {
                    ruleData.setOutputExpValue(true);
                    outPutExpButton.setSelection(true);
                    outPutExpButton.setEnabled(false);
                    for (var exp : expressionList) {
                        exp.setValue("");
                    }
                    valueColumn.getColumn().setText("Output Expression");
                } else {
                    outPutExpButton.setEnabled(true);
                }
                if (!ruleData.isOutputExpValue()) {
                    for (var exp : expressionList) {
                        exp.setValue(ruleData.isOutputExpValue() ? "" : ruleData.getProperty().getDefaultValue());
                    }
                }
                expressionViewer.refresh();
            }
        });

        outPutExpButton = new Button(topComposite, SWT.CHECK);
        gd = new GridData();
        gd.horizontalSpan = 2;
        outPutExpButton.setLayoutData(gd);
        outPutExpButton.setText("Output Expression");
        if (ruleData.getProperty().getPropertyDescriptor() == null
                || ruleData.getProperty().onlyAcceptExpressionInRule()) {
            ruleData.setOutputExpValue(true);
            outPutExpButton.setEnabled(false);
        }
        outPutExpButton.setSelection(ruleData.isOutputExpValue());
        outPutExpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ruleData.setOutputExpValue(outPutExpButton.getSelection());
                for (var exp : expressionList) {
                    exp.setValue(ruleData.isOutputExpValue() ? "" : ruleData.getProperty().getDefaultValue());
                }
                valueColumn.getColumn().setText(ruleData.isOutputExpValue() ? "Output Expression" : "Output Value");
                expressionViewer.refresh();
            }
        });

        // Left Panel: List of scripts
        var leftComposite = new Composite(mainComposite, SWT.NONE);
        leftComposite.setLayout(new GridLayout(1, false));
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 350;
        leftComposite.setLayoutData(gd);
        createLabel(leftComposite, "Expressions");

        var toolBarComposite = new Composite(leftComposite, SWT.NONE);
        var gridLayout = new GridLayout(1, false);
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        toolBarComposite.setLayout(gridLayout);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        toolBarComposite.setLayoutData(gd);

        var toolbarManager = new ToolBarManager(SWT.FLAT);
        var toolBar = toolbarManager.createControl(toolBarComposite);
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

        expressionViewer = createExpressionsTableViewer(toolBarComposite);
        expressionViewer.setInput(expressionList);
        expressionViewer.addSelectionChangedListener(event -> refreshActionBarOnSelection());

        // Right panel: Input PVs for selected script
        var rightComposite = new Composite(mainComposite, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        rightComposite.setLayout(gridLayout);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumWidth = 250; // Account for the StringTableEditor's minimum size
        rightComposite.setLayoutData(gd);
        this.createLabel(rightComposite, "Input PVs");

        pvsEditor = new PVTupleTableEditor(rightComposite, ruleData.getPVList(), SWT.NONE);
        pvsEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (expressionList.size() > 0) {
            setExpressionViewerSelection(expressionList.get(0));
        }

        var bottomComposite = new Composite(mainComposite, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(1, false));
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        bottomComposite.setLayoutData(gd);

        var generateScriptButton = new Button(bottomComposite, SWT.PUSH);
        generateScriptButton.setText("See Generated Script");
        gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
        generateScriptButton.setLayoutData(gd);

        var scriptText = new Text(bottomComposite,
                SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        scriptText.setLayoutData(gd);

        generateScriptButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                scriptText.setText(ruleData.generateScript());
            }
        });
        return parentComposite;
    }

    /**
     * Refreshes the enabled-state of the actions.
     */
    private void refreshActionBarOnSelection() {

        var selection = (IStructuredSelection) expressionViewer.getSelection();
        var enabled = !selection.isEmpty() && selection.getFirstElement() instanceof Expression;
        copyAction.setEnabled(enabled);
        removeAction.setEnabled(enabled);
        moveUpAction.setEnabled(enabled);
        moveDownAction.setEnabled(enabled);
    }

    private void setExpressionViewerSelection(Expression expression) {
        expressionViewer.refresh();
        if (expression == null) {
            expressionViewer.setSelection(StructuredSelection.EMPTY);
        } else {
            expressionViewer.setSelection(new StructuredSelection(expression));
        }
    }

    /**
     * Creates and configures a {@link TableViewer}.
     *
     * @param parent
     *            The parent for the table
     * @return The {@link TableViewer}
     */
    private TableViewer createExpressionsTableViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        var expressionColumn = new TableViewerColumn(viewer, SWT.NONE);
        expressionColumn.getColumn().setText("Boolean Expression");
        expressionColumn.getColumn().setMoveable(false);
        expressionColumn.getColumn().setWidth(200);
        expressionColumn.setEditingSupport(new EditingSupport(viewer) {
            @Override
            protected void setValue(Object element, Object value) {
                if (element instanceof Expression) {
                    ((Expression) element).setBooleanExpression(value.toString());
                }
                viewer.refresh();
            }

            @Override
            protected Object getValue(Object element) {
                if (element instanceof Expression) {
                    return ((Expression) element).getBooleanExpression();
                }
                return null;
            }

            @Override
            protected CellEditor getCellEditor(Object element) {
                return new TextCellEditor(viewer.getTable());
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }
        });

        valueColumn = new TableViewerColumn(viewer, SWT.NONE);
        valueColumn.getColumn().setText(ruleData.isOutputExpValue() ? "Output Expression" : "Output Value");
        valueColumn.getColumn().setMoveable(false);
        valueColumn.getColumn().setWidth(200);
        EditingSupport editingSupport = new EditingSupport(viewer) {
            @Override
            protected void setValue(Object element, Object value) {
                if (element instanceof Expression) {
                    ((Expression) element).setValue(value);
                }
                viewer.refresh();
            }

            @Override
            protected Object getValue(Object element) {
                if (element instanceof Expression) {
                    if (((Expression) element).getValue() == null) {
                        return "";
                    }
                    return ((Expression) element).getValue();
                }
                return null;
            }

            @Override
            protected CellEditor getCellEditor(Object element) {
                if (element instanceof Expression) {
                    if (ruleData.isOutputExpValue() || ruleData.getProperty().getPropertyDescriptor() == null) {
                        return new TextCellEditor(viewer.getTable());
                    } else {
                        return ruleData.getProperty().getPropertyDescriptor().createPropertyEditor(viewer.getTable());
                    }
                }
                return null;
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }
        };
        valueColumn.setEditingSupport(editingSupport);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new ExpressionLabelProvider());
        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        viewer.getTable().setLayoutData(gd);
        return viewer;
    }

    /**
     * Creates the actions.
     */
    private void createActions() {
        addAction = new Action("Add") {
            @Override
            public void run() {
                var expression = new Expression("",
                        ruleData.isOutputExpValue() ? "" : ruleData.getProperty().getDefaultValue());
                expressionList.add(expression);
                expressionViewer.refresh();
                setExpressionViewerSelection(expression);
            }
        };
        addAction.setToolTipText("Add an Expression");
        addAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/add.gif"));

        copyAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) expressionViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof Expression) {
                    var expression = ((Expression) selection.getFirstElement()).getCopy();
                    expressionList.add(expression);
                    setExpressionViewerSelection(expression);
                }
            }
        };
        copyAction.setText("Copy");
        copyAction.setToolTipText("Copy selected expression");
        copyAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/copy.gif"));
        copyAction.setEnabled(false);

        removeAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) expressionViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof Expression) {
                    expressionList.remove((Expression) selection.getFirstElement());
                    setExpressionViewerSelection(null);
                    this.setEnabled(false);
                }
            }
        };
        removeAction.setText("Remove Expression");
        removeAction.setToolTipText("Remove the selected expression from the list");
        removeAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/delete.gif"));
        removeAction.setEnabled(false);

        moveUpAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) expressionViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof Expression) {
                    var expression = (Expression) selection.getFirstElement();
                    var i = expressionList.indexOf(expression);
                    if (i > 0) {
                        expressionList.remove(expression);
                        expressionList.add(i - 1, expression);
                        setExpressionViewerSelection(expression);
                    }
                }
            }
        };
        moveUpAction.setText("Move Expression Up");
        moveUpAction.setToolTipText("Move selected expression up");
        moveUpAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_prev.gif"));
        moveUpAction.setEnabled(false);

        moveDownAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) expressionViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof Expression) {
                    var expression = (Expression) selection.getFirstElement();
                    var i = expressionList.indexOf(expression);
                    if (i < expressionList.size() - 1) {
                        expressionList.remove(expression);
                        expressionList.add(i + 1, expression);
                        setExpressionViewerSelection(expression);
                    }
                }
            }
        };
        moveDownAction.setText("Move Expression Down");
        moveDownAction.setToolTipText("Move selected expression down");
        moveDownAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_next.gif"));
        moveDownAction.setEnabled(false);
    }

    class ExpressionLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 1 && !ruleData.isOutputExpValue() && element instanceof Expression) {
                var expression = (Expression) element;

                if (ruleData.getProperty().getPropertyDescriptor() == null) {
                    return null;
                }
                if (ruleData.getProperty().getPropertyDescriptor().getLabelProvider() != null) {
                    return ruleData.getProperty().getPropertyDescriptor().getLabelProvider()
                            .getImage(expression.getValue());
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element != null && element instanceof Expression) {
                var expression = (Expression) element;
                if (columnIndex == 0) {
                    return expression.getBooleanExpression();
                }

                if (ruleData.getProperty().getPropertyDescriptor() != null && !ruleData.isOutputExpValue()
                        && ruleData.getProperty().getPropertyDescriptor().getLabelProvider() != null) {
                    return ruleData.getProperty().getPropertyDescriptor().getLabelProvider()
                            .getText(expression.getValue());
                } else if (expression.getValue() == null) {
                    return "";
                } else {
                    return expression.getValue().toString();
                }
            }
            if (element != null) {
                return element.toString();
            }
            return "error";
        }
    }
}
