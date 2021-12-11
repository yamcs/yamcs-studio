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
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.widgets.editparts.XYGraphEditPart;
import org.csstudio.opibuilder.widgets.model.XYGraphModel;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;

/**
 * Show/Hide XYGraph Toolbar
 */
public class ShowXYGraphToolbarAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {
        Command command = new SetWidgetPropertyCommand(getSelectedXYGraph().getWidgetModel(),
                XYGraphModel.PROP_SHOW_TOOLBAR, !getSelectedXYGraph().getWidgetModel().isShowToolbar());
        execute(command);

    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final XYGraphEditPart getSelectedXYGraph() {
        return (XYGraphEditPart) selection.getFirstElement();
    }
}
