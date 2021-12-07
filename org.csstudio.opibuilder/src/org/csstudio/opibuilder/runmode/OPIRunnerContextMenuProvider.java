/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import java.util.List;

import org.csstudio.opibuilder.actions.ConfigureRuntimePropertiesAction;
import org.csstudio.opibuilder.actions.OpenRelatedDisplayAction;
import org.csstudio.opibuilder.actions.OpenRelatedDisplayAction.OpenDisplayTarget;
import org.csstudio.opibuilder.actions.WidgetActionMenuAction;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.util.WorkbenchWindowService;
import org.csstudio.opibuilder.widgetActions.AbstractWidgetAction;
import org.csstudio.opibuilder.widgetActions.OpenDisplayAction;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * ContextMenuProvider implementation for the OPI Runner.
 */
public final class OPIRunnerContextMenuProvider extends ContextMenuProvider {

    private IOPIRuntime opiRuntime;

    public OPIRunnerContextMenuProvider(EditPartViewer viewer, IOPIRuntime opiRuntime) {
        super(viewer);
        this.opiRuntime = opiRuntime;
    }

    @Override
    public void buildContextMenu(IMenuManager menu) {
        addSettingPropertiesAction(menu);
        addWidgetActionToMenu(menu);
        GEFActionConstants.addStandardActionGroups(menu);

        var actionRegistry = (ActionRegistry) opiRuntime.getAdapter(ActionRegistry.class);
        var action = actionRegistry.getAction(ActionFactory.REFRESH.getId());
        if (action != null) {
            menu.appendToGroup(GEFActionConstants.GROUP_PRINT, action);
        }

        var activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        // Only show 'full screen' option for OPIView, not for OPIShell.
        if (opiRuntime instanceof OPIView || opiRuntime instanceof OPIRunner) {
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
                    WorkbenchWindowService.getInstance().getFullScreenAction(activeWindow));
        }

        // ELog and EMail actions may not be available
        action = actionRegistry.getAction(ActionFactory.PRINT.getId());
        if (action != null) {
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        }
    }

    @Override
    protected boolean allowItem(IContributionItem itemToAdd) {
        // org.eclipse.wst.sse.ui adds some junk, which we don't need
        if ("sourceMenuId".equals(itemToAdd.getId())) {
            return false;
        }
        return super.allowItem(itemToAdd);
    }

    /**
     * Adds the defined {@link AbstractWidgetAction}s to the given {@link IMenuManager}.
     * 
     * @param menu
     *            The {@link IMenuManager}
     */
    private void addWidgetActionToMenu(IMenuManager menu) {
        List<?> selectedEditParts = ((IStructuredSelection) getViewer().getSelection()).toList();
        if (selectedEditParts.size() == 1) {
            if (selectedEditParts.get(0) instanceof AbstractBaseEditPart) {
                var editPart = (AbstractBaseEditPart) selectedEditParts.get(0);
                var widget = editPart.getWidgetModel();

                // add menu Open, Open in New Tab and Open in New Window
                var hookedActions = editPart.getHookedActions();

                if (hookedActions != null && hookedActions.size() == 1) {
                    var hookedAction = hookedActions.get(0);
                    if (hookedAction instanceof OpenDisplayAction) {
                        var original_action = (OpenDisplayAction) hookedAction;
                        menu.add(new OpenRelatedDisplayAction(original_action, OpenDisplayTarget.DEFAULT));
                        menu.add(new OpenRelatedDisplayAction(original_action, OpenDisplayTarget.NEW_TAB));
                        menu.add(new OpenRelatedDisplayAction(original_action, OpenDisplayTarget.NEW_WINDOW));
                        menu.add(new OpenRelatedDisplayAction(original_action, OpenDisplayTarget.NEW_SHELL));
                    }
                }

                var ai = widget.getActionsInput();
                if (ai != null) {
                    List<AbstractWidgetAction> widgetActions = ai.getActionsList();
                    if (!widgetActions.isEmpty()) {
                        var actionMenu = new MenuManager("Actions", "actions");
                        for (AbstractWidgetAction action : widgetActions) {
                            actionMenu.add(new WidgetActionMenuAction(action));
                        }
                        menu.add(actionMenu);
                    }
                }
            }
        }
    }

    private void addSettingPropertiesAction(IMenuManager menu) {
        List<?> selectedEditParts = ((IStructuredSelection) getViewer().getSelection()).toList();
        if (selectedEditParts.size() == 1) {
            if (selectedEditParts.get(0) instanceof AbstractBaseEditPart) {
                var editPart = (AbstractBaseEditPart) selectedEditParts.get(0);
                var widget = editPart.getWidgetModel();
                if (widget.getRuntimePropertyList() != null) {
                    menu.add(new ConfigureRuntimePropertiesAction(getViewer().getControl().getShell(), widget));
                }
            }
        }
    }
}
