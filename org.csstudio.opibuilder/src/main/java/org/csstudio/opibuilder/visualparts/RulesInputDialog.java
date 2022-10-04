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

import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.script.RuleData;
import org.csstudio.opibuilder.script.RulesInput;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The dialog for rules input editing.
 */
public class RulesInputDialog extends TrayDialog {

    private Action addAction;
    private Action editAction;
    private Action copyAction;
    private Action removeAction;
    private Action moveUpAction;
    private Action moveDownAction;

    private ListViewer rulesViewer;

    private List<RuleData> ruleDataList;
    private String title;
    private AbstractWidgetModel widgetModel;

    public RulesInputDialog(Shell parentShell, RulesInput scriptsInput, AbstractWidgetModel widgetModel,
            String dialogTitle) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        ruleDataList = scriptsInput.getCopy().getRuleDataList();
        title = dialogTitle;
        this.widgetModel = widgetModel;
    }

    @Override
    protected void okPressed() {
        for (var ruleData : ruleDataList) {
            var hasTrigger = false;
            for (var pvTuple : ruleData.getPVList()) {
                hasTrigger |= pvTuple.trigger;
            }
            if (!hasTrigger) {
                MessageDialog.openWarning(getShell(), "Warning", NLS.bind(
                        "At least one trigger PV must be selected for the rule:\n{0}", ruleData.getName().toString()));
                return;
            }
        }
        super.okPressed();
    }

    public final List<RuleData> getRuleDataList() {
        return ruleDataList;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
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
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        toolBar.setLayoutData(gd);
        createActions();
        toolbarManager.add(addAction);
        toolbarManager.add(editAction);
        toolbarManager.add(copyAction);
        toolbarManager.add(removeAction);
        toolbarManager.add(moveUpAction);
        toolbarManager.add(moveDownAction);

        toolbarManager.update(true);

        var mainComposite = new Composite(topComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        mainComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 200;
        mainComposite.setLayoutData(gd);

        rulesViewer = createRulesListViewer(mainComposite);
        rulesViewer.setInput(ruleDataList);
        rulesViewer.addSelectionChangedListener(event -> refreshToolbarOnSelection());

        return parentComposite;
    }

    private void setRulesViewerSelection(RuleData ruleData) {
        rulesViewer.refresh();
        if (ruleData == null) {
            rulesViewer.setSelection(StructuredSelection.EMPTY);
        } else {
            rulesViewer.setSelection(new StructuredSelection(ruleData));
        }
        refreshToolbarOnSelection();
    }

    private void refreshToolbarOnSelection() {
        var enabled = !rulesViewer.getSelection().isEmpty();
        removeAction.setEnabled(enabled);
        editAction.setEnabled(enabled);
        copyAction.setEnabled(enabled);
        moveUpAction.setEnabled(enabled);
        moveDownAction.setEnabled(enabled);
    }

    private ListViewer createRulesListViewer(Composite parent) {
        var viewer = new ListViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
        viewer.setContentProvider(new BaseWorkbenchContentProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public Object[] getElements(Object element) {
                return (((List<RuleData>) element).toArray());
            }
        });
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.addDoubleClickListener(event -> invokeRuleDataDialog());
        viewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return viewer;
    }

    private void createActions() {
        addAction = new Action("Add") {
            @Override
            public void run() {
                var dialog = new RuleDataEditDialog(getShell(), new RuleData(widgetModel));
                if (dialog.open() == OK) {
                    ruleDataList.add(dialog.getOutput());
                    rulesViewer.refresh();
                }
            }
        };
        addAction.setToolTipText("Add a Rule");
        addAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/add.gif"));

        editAction = new Action("Edit") {
            @Override
            public void run() {
                invokeRuleDataDialog();
            }
        };
        editAction.setToolTipText("Edit Selected Rule");
        editAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/edit.gif"));
        editAction.setEnabled(false);

        copyAction = new Action("Copy") {
            @Override
            public void run() {
                var selection = (IStructuredSelection) rulesViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof RuleData) {
                    var ruleData = ((RuleData) selection.getFirstElement()).getCopy();
                    ruleDataList.add(ruleData);
                    setRulesViewerSelection(ruleData);
                }
            }
        };
        copyAction.setToolTipText("Copy Selected Rule");
        copyAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/copy.gif"));
        copyAction.setEnabled(false);

        removeAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) rulesViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof RuleData) {
                    ruleDataList.remove(selection.getFirstElement());
                    setRulesViewerSelection(null);
                    setEnabled(false);
                }
            }
        };
        removeAction.setToolTipText("Remove Selected Rule");
        removeAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/delete.gif"));
        removeAction.setEnabled(false);

        moveUpAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) rulesViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof RuleData) {
                    var ruleData = (RuleData) selection.getFirstElement();
                    var i = ruleDataList.indexOf(ruleData);
                    if (i > 0) {
                        ruleDataList.remove(ruleData);
                        ruleDataList.add(i - 1, ruleData);
                        setRulesViewerSelection(ruleData);
                    }
                }
            }
        };
        moveUpAction.setText("Move Rule Up");
        moveUpAction.setToolTipText("Move Selected Rule up");
        moveUpAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_prev.gif"));
        moveUpAction.setEnabled(false);

        moveDownAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) rulesViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof RuleData) {
                    var ruleData = (RuleData) selection.getFirstElement();
                    var i = ruleDataList.indexOf(ruleData);
                    if (i < ruleDataList.size() - 1) {
                        ruleDataList.remove(ruleData);
                        ruleDataList.add(i + 1, ruleData);
                        setRulesViewerSelection(ruleData);
                    }
                }
            }
        };
        moveDownAction.setText("Move Rule Down");
        moveDownAction.setToolTipText("Move Selected Rule Down");
        moveDownAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_next.gif"));
        moveDownAction.setEnabled(false);
    }

    private void invokeRuleDataDialog() {
        var selection = (RuleData) ((IStructuredSelection) rulesViewer.getSelection()).getFirstElement();
        if (selection == null) {
            return;
        }
        var dialog = new RuleDataEditDialog(rulesViewer.getControl().getShell(), selection);
        if (dialog.open() == OK) {
            var result = dialog.getOutput();
            var index = ruleDataList.indexOf(selection);
            ruleDataList.remove(index);
            ruleDataList.add(index, result);
            rulesViewer.refresh();
        }
    }
}
