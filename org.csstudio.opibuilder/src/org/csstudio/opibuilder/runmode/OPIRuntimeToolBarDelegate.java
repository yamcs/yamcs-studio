/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.csstudio.opibuilder.actions.NavigateOPIsAction;
import org.csstudio.opibuilder.actions.PartZoomInAction;
import org.csstudio.opibuilder.actions.PartZoomOutAction;
import org.csstudio.opibuilder.visualparts.PartZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;

/**
 * The toolbar contributor for OPI runner
 */
public class OPIRuntimeToolBarDelegate {

    /**
     * The action bars; <code>null</code> until <code>init</code> is called.
     */
    private IActionBars bars;

    /**
     * The workbench page; <code>null</code> until <code>init</code> is called.
     */
    private IWorkbenchPage page;

    private IToolBarManager toolbar;

    private NavigateOPIsAction backwardAction, forwardAction;
    private PartZoomInAction partZoomInAction;
    private PartZoomOutAction partZoomOutAction;
    private PartZoomComboContributionItem partZoomComboContributionItem;

    public void init(IActionBars bars, IWorkbenchPage page) {
        this.page = page;
        this.bars = bars;
        backwardAction = new NavigateOPIsAction(false);
        forwardAction = new NavigateOPIsAction(true);
        partZoomInAction = new PartZoomInAction();
        partZoomOutAction = new PartZoomOutAction();
        partZoomComboContributionItem = new PartZoomComboContributionItem(page);
    }

    public void contributeToToolBar(IToolBarManager toolBarManager) {
        toolbar = toolBarManager;
        toolBarManager.add(partZoomInAction);
        toolBarManager.add(partZoomOutAction);
        toolBarManager.add(partZoomComboContributionItem);
        toolBarManager.add(backwardAction);
        toolBarManager.add(forwardAction);
    }

    /**
     * Hook {@link IOPIRuntime} with this toolbar.
     * 
     * @param opiRuntime
     */
    public void setActiveOPIRuntime(IOPIRuntime opiRuntime) {

        partZoomInAction.setPart(opiRuntime);
        partZoomOutAction.setPart(opiRuntime);
        partZoomComboContributionItem.setPart(opiRuntime);
        var manager = opiRuntime.getAdapter(DisplayOpenManager.class);
        backwardAction.setDisplayOpenManager(manager);
        forwardAction.setDisplayOpenManager(manager);
        var bars = getActionBars();
        bars.setGlobalActionHandler(backwardAction.getId(), backwardAction);
        bars.setGlobalActionHandler(forwardAction.getId(), forwardAction);

        var actionRegistry = opiRuntime.getAdapter(ActionRegistry.class);
        bars.setGlobalActionHandler(ActionFactory.PRINT.getId(), actionRegistry.getAction(ActionFactory.PRINT.getId()));
        bars.setGlobalActionHandler(ActionFactory.REFRESH.getId(),
                actionRegistry.getAction(ActionFactory.REFRESH.getId()));
        bars.setGlobalActionHandler(partZoomInAction.getId(), partZoomInAction);
        bars.setGlobalActionHandler(partZoomOutAction.getId(), partZoomOutAction);
        bars.updateActionBars();

    }

    /**
     * Returns this contributor's workbench page.
     *
     * @return the workbench page
     */
    public IWorkbenchPage getPage() {
        return page;
    }

    /**
     * Returns this contributor's action bars.
     *
     * @return the action bars
     */
    public IActionBars getActionBars() {
        return bars;
    }

    /**
     * Disposes of the elements which are not disposed automatically.
     */
    public void dispose() {
        if (toolbar != null) {
            toolbar.remove(partZoomInAction.getId());
            toolbar.remove(partZoomOutAction.getId());
            toolbar.remove(partZoomComboContributionItem);
            toolbar.remove(backwardAction.getId());
            toolbar.remove(forwardAction.getId());
        }
        backwardAction.dispose();
        forwardAction.dispose();
        partZoomComboContributionItem.dispose();
        partZoomInAction.dispose();
        partZoomOutAction.dispose();
    }
}
