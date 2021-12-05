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
 * Change tab index.
 */
public class ChangeTabIndexCommand extends Command {
    private int newIndex, oldIndex;
    private TabModel tabModel;
    private TabItem tabItem;

    public ChangeTabIndexCommand(TabEditPart tabEditPart, int newIndex) {
        this.tabModel = tabEditPart.getWidgetModel();
        this.oldIndex = tabEditPart.getActiveTabIndex();
        this.newIndex = newIndex;

        this.tabItem = tabEditPart.getTabItem(oldIndex);

        setLabel("Change Tab Index");
    }

    @Override
    public void execute() {
        tabModel.removeTab(oldIndex);
        tabModel.addTab(newIndex, tabItem);
    }

    @Override
    public void undo() {
        tabModel.removeTab(newIndex);
        tabModel.addTab(oldIndex, tabItem);
    }

}
