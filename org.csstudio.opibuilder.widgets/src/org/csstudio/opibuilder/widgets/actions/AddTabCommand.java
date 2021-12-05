/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.actions;

import org.csstudio.opibuilder.widgets.editparts.TabEditPart;
import org.csstudio.opibuilder.widgets.editparts.TabItem;
import org.csstudio.opibuilder.widgets.model.TabModel;
import org.eclipse.gef.commands.Command;

/**
 * The command which add a tab to the tab widget.
 */
public class AddTabCommand extends Command {
    private int tabIndex;
    private TabModel tabModel;
    private TabItem tabItem = null;

    public AddTabCommand(TabEditPart tabEditPart, boolean before) {
        this.tabModel = tabEditPart.getWidgetModel();
        if (before)
            this.tabIndex = tabEditPart.getActiveTabIndex();
        else
            this.tabIndex = tabEditPart.getActiveTabIndex() + 1;
        setLabel("Add Tab");
    }

    @Override
    public void execute() {
        if (tabItem == null)
            tabItem = new TabItem(tabModel, tabIndex);
        tabModel.addTab(tabIndex, tabItem);
    }

    @Override
    public void undo() {
        tabModel.removeTab(tabIndex);
    }

}
