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

import org.csstudio.opibuilder.actions.AbstractWidgetTargetAction;
import org.csstudio.opibuilder.widgets.editparts.TabEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;

/**
 * Remove the current tab.
 */
public class RemoveTabAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {
        Command command = new RemoveTabCommand(getSelectedTabWidget());
        execute(command);

    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final TabEditPart getSelectedTabWidget() {
        return (TabEditPart) selection.getFirstElement();
    }
}
