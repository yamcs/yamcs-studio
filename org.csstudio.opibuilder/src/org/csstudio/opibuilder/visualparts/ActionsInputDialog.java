/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.opibuilder.widgetActions.AbstractWidgetAction;
import org.csstudio.opibuilder.widgetActions.ActionsInput;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The dialog to configure actions input.
 */
public class ActionsInputDialog extends TrayDialog {

    private Action addAction;
    private Action copyAction;
    private Action removeAction;
    private Action moveUpAction;
    private Action moveDownAction;

    private TableViewer actionsViewer;

    private TableViewer propertiesViewer;

    private LinkedList<AbstractWidgetAction> actionsList;
    private boolean hookedUpFirstActionToWidget;
    private boolean hookedUpAllActionsToWidget;

    private boolean showHookOption = true;
    private ActionsInput actionsInput;
    private String title;
    private Button hookFirstCheckBox;

    public ActionsInputDialog(Shell parentShell, ActionsInput actionsInput, String dialogTitle,
            boolean showHookOption) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.actionsInput = actionsInput.getCopy();
        this.actionsList = this.actionsInput.getActionsList();
        hookedUpFirstActionToWidget = actionsInput.isFirstActionHookedUpToWidget();
        hookedUpAllActionsToWidget = actionsInput.isHookUpAllActionsToWidget();
        title = dialogTitle;
        this.showHookOption = showHookOption;
    }

    public ActionsInput getOutput() {
        var actionsInput = new ActionsInput(actionsList);
        actionsInput.setHookUpFirstActionToWidget(hookedUpFirstActionToWidget);
        actionsInput.setHookUpAllActionsToWidget(hookedUpAllActionsToWidget);
        return actionsInput;
    }

    @Override
    protected void okPressed() {
        propertiesViewer.getTable().forceFocus();
        super.okPressed();
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
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 250;
        mainComposite.setLayoutData(gd);
        var leftComposite = new Composite(mainComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        leftComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 250;
        leftComposite.setLayoutData(gd);

        actionsViewer = createActionsTableViewer(leftComposite);
        actionsViewer.setInput(actionsList);

        var rightComposite = new Composite(mainComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        rightComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 350;
        rightComposite.setLayoutData(gd);

        propertiesViewer = createPropertiesViewer(rightComposite);
        var bottomComposite = new Composite(mainComposite, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(1, false));
        bottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        if (showHookOption) {
            hookFirstCheckBox = new Button(bottomComposite, SWT.CHECK);
            hookFirstCheckBox.setSelection(hookedUpFirstActionToWidget);
            hookFirstCheckBox.setText("Hook the first action to the mouse click event on widget.");
            hookFirstCheckBox.setEnabled(!hookedUpAllActionsToWidget);
            hookFirstCheckBox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    hookedUpFirstActionToWidget = hookFirstCheckBox.getSelection();
                }
            });
        }

        var hookAllCheckBox = new Button(bottomComposite, SWT.CHECK);
        hookAllCheckBox.setSelection(hookedUpAllActionsToWidget);
        hookAllCheckBox.setText("Hook all actions to the mouse click event on widget.");
        hookAllCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                hookedUpAllActionsToWidget = hookAllCheckBox.getSelection();
                if (hookFirstCheckBox != null) {
                    hookFirstCheckBox.setEnabled(!hookedUpAllActionsToWidget);
                }
            }
        });

        if (actionsList.size() > 0) {
            refreshActionsViewer(actionsList.get(0));
        }

        return parentComposite;
    }

    private TableViewer createPropertiesViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        var tvColumn = new TableViewerColumn(viewer, SWT.NONE);
        tvColumn.getColumn().setText("Property");
        tvColumn.getColumn().setMoveable(false);
        tvColumn.getColumn().setWidth(100);
        tvColumn = new TableViewerColumn(viewer, SWT.NONE);
        tvColumn.getColumn().setText("Value");
        tvColumn.getColumn().setMoveable(false);
        tvColumn.getColumn().setWidth(300);

        var editingSupport = new PropertiesEditingSupport(viewer, viewer.getTable());
        tvColumn.setEditingSupport(editingSupport);

        viewer.setContentProvider(new WidgetPropertiesContentProvider());
        viewer.setLabelProvider(new PropertiesLabelProvider());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.getTable().setEnabled(false);
        return viewer;
    }

    /**
     * Refreshes the enabled-state of the actions.
     */
    private void refreshGUIOnSelection() {
        var selection = (IStructuredSelection) actionsViewer.getSelection();
        if (!selection.isEmpty() && selection.getFirstElement() instanceof AbstractWidgetAction) {
            removeAction.setEnabled(true);
            moveUpAction.setEnabled(true);
            moveDownAction.setEnabled(true);
            copyAction.setEnabled(true);
            propertiesViewer.setInput(((AbstractWidgetAction) selection.getFirstElement()).getAllProperties());
            propertiesViewer.getTable().setEnabled(true);
        } else {
            removeAction.setEnabled(false);
            moveUpAction.setEnabled(false);
            moveDownAction.setEnabled(false);
            propertiesViewer.getTable().setEnabled(false);
            copyAction.setEnabled(false);
        }
    }

    private void refreshActionsViewer(AbstractWidgetAction widgetAction) {
        actionsViewer.refresh();
        if (widgetAction == null) {
            actionsViewer.setSelection(StructuredSelection.EMPTY);
        } else {
            actionsViewer.setSelection(new StructuredSelection(widgetAction));
        }
    }

    /**
     * Creates and configures a {@link TableViewer}.
     *
     * @param parent
     *            The parent for the table
     * @return The {@link TableViewer}
     */
    private TableViewer createActionsTableViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
        viewer.setContentProvider(new BaseWorkbenchContentProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public Object[] getElements(Object element) {
                return (((List<AbstractWidgetAction>) element).toArray());
            }
        });
        viewer.setLabelProvider(new WorkbenchLabelProvider() {
            @Override
            protected String decorateText(String input, Object element) {
                return input + "(index: " + actionsList.indexOf(element) + ")";
            }
        });
        viewer.addSelectionChangedListener(event -> refreshGUIOnSelection());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return viewer;
    }

    /**
     * Creates the popup-menu for adding a {@link AbstractWidgetActionModel}.
     *
     * @param control
     *            The {@link Control} for the menu
     * @param withRemoveAction
     *            Indicates if an action to remove a {@link AbstractWidgetActionModel} should be added
     * @return The resulting menu
     */
    private Menu createMenu(Control control, boolean withRemoveAction) {
        var listMenu = new MenuManager();
        for (var type : ActionType.values()) {
            listMenu.add(new MenuAction(type));
        }
        if (withRemoveAction) {
            listMenu.add(new Separator());
            listMenu.add(removeAction);
        }
        return listMenu.createContextMenu(control);
    }

    private void createActions() {
        addAction = new Action("Add") {
            @Override
            public void run() {
            }
        };
        addAction.setMenuCreator(new IMenuCreator() {

            private Menu menu;

            @Override
            public void dispose() {
                if (menu != null) {
                    menu.dispose();
                    menu = null;
                }
            }

            @Override
            public Menu getMenu(Control parent) {
                if (menu != null) {
                    menu.dispose();
                }
                menu = createMenu(parent, false);
                return menu;
            }

            @Override
            public Menu getMenu(Menu parent) {
                return null;
            }

        });

        addAction.setToolTipText("Add an action");
        addAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/add.gif"));

        copyAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) actionsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof AbstractWidgetAction) {
                    var newAction = ((AbstractWidgetAction) selection.getFirstElement()).getCopy();
                    actionsInput.addAction(newAction);
                    actionsViewer.setSelection(new StructuredSelection(newAction));
                    refreshActionsViewer(newAction);
                }
            }
        };
        copyAction.setText("Copy Action");
        copyAction.setToolTipText("Copy the selected action");
        copyAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/copy.gif"));
        copyAction.setEnabled(false);

        removeAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) actionsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof AbstractWidgetAction) {
                    actionsList.remove((AbstractWidgetAction) selection.getFirstElement());
                    refreshActionsViewer(null);
                    this.setEnabled(false);
                }
            }
        };
        removeAction.setText("Remove Action");
        removeAction.setToolTipText("Remove the selected action from the list");
        removeAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/delete.gif"));
        removeAction.setEnabled(false);

        moveUpAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) actionsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof AbstractWidgetAction) {
                    var widgetAction = (AbstractWidgetAction) selection.getFirstElement();
                    var i = actionsList.indexOf(widgetAction);
                    if (i > 0) {
                        actionsList.remove(widgetAction);
                        actionsList.add(i - 1, widgetAction);
                        refreshActionsViewer(widgetAction);
                    }
                }
            }
        };
        moveUpAction.setText("Move Up Action");
        moveUpAction.setToolTipText("Move up the selected action");
        moveUpAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_prev.gif"));
        moveUpAction.setEnabled(false);

        moveDownAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) actionsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof AbstractWidgetAction) {
                    var widgetAction = (AbstractWidgetAction) selection.getFirstElement();
                    var i = actionsList.indexOf(widgetAction);
                    if (i < actionsList.size() - 1) {
                        actionsList.remove(widgetAction);
                        actionsList.add(i + 1, widgetAction);
                        refreshActionsViewer(widgetAction);
                    }
                }
            }
        };
        moveDownAction.setText("Move Down Action");
        moveDownAction.setToolTipText("Move down the selected action");
        moveDownAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_next.gif"));
        moveDownAction.setEnabled(false);
    }

    /**
     * An {@link Action}, which adds a new {@link AbstractWidgetAction} of the given {@link ActionType}.
     */
    private final class MenuAction extends Action {
        private ActionType type;

        public MenuAction(ActionType type) {
            this.type = type;
            this.setText("Add " + type.getDescription());
            this.setImageDescriptor(type.getIconImage());
        }

        @Override
        public void run() {
            var widgetAction = WidgetActionFactory.createWidgetAction(type);
            if (widgetAction != null) {
                actionsInput.addAction(widgetAction);
                refreshActionsViewer(widgetAction);
            }

        }
    }

    final static class WidgetPropertiesContentProvider extends ArrayContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof AbstractWidgetProperty[]) {
                var oldProperties = (AbstractWidgetProperty[]) inputElement;
                var newPropertiesList = new ArrayList<AbstractWidgetProperty>();
                for (var property : oldProperties) {
                    if (property.isVisibleInPropSheet()) {
                        newPropertiesList.add(property);
                    }
                }

                return newPropertiesList.toArray();
            }
            return super.getElements(inputElement);
        }
    }
}
