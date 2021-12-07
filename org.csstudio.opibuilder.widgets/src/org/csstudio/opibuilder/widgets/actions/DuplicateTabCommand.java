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

import java.util.logging.Level;

import org.csstudio.opibuilder.widgets.Activator;
import org.csstudio.opibuilder.widgets.editparts.TabEditPart;
import org.csstudio.opibuilder.widgets.editparts.TabItem;
import org.csstudio.opibuilder.widgets.model.TabModel;
import org.eclipse.gef.commands.Command;

/**
 * Duplicate a tab
 */
public class DuplicateTabCommand extends Command {
    final private int tabIndex;
    final private TabModel tabModel;
    private TabItem tabItem;

    public DuplicateTabCommand(TabEditPart tabEditPart) {
        this.tabModel = tabEditPart.getWidgetModel();
        this.tabIndex = tabEditPart.getActiveTabIndex() + 1;
        try {
            this.tabItem = tabEditPart.getTabItem(tabIndex - 1).getCopy();
        } catch (Exception e) {
            var message = "Failed to duplicate tab";
            Activator.getLogger().log(Level.SEVERE, message, e);
        }
        setLabel("Duplicate Tab");
    }

    @Override
    public void execute() {
        tabModel.addTab(tabIndex, tabItem);
    }

    @Override
    public void undo() {
        tabModel.removeTab(tabIndex);
    }
}
