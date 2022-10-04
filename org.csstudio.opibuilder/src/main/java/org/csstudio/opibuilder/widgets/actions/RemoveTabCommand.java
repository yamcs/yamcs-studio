/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
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
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * The command which add a tab to the tab widget.
 */
public class RemoveTabCommand extends Command {
    private int tabIndex;
    private TabModel tabModel;

    private TabItem tabItem;

    private boolean executed = false;

    public RemoveTabCommand(TabEditPart tabEditPart) {
        tabModel = tabEditPart.getWidgetModel();
        tabIndex = tabEditPart.getActiveTabIndex();
        tabItem = tabEditPart.getTabItem(tabIndex);
        setLabel("Remove Tab");
    }

    @Override
    public void execute() {
        if (tabModel.getChildren().size() > 1) {
            tabModel.removeTab(tabIndex);
            executed = true;
        } else {
            MessageDialog.openInformation(null, "Failed to Remove Tab",
                    "There must be at least one tab in the tab folder.");
        }
    }

    @Override
    public void undo() {
        if (executed) {
            tabModel.addTab(tabIndex, tabItem);
        }
        executed = false;
    }
}
