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

import org.csstudio.opibuilder.actions.AbstractWidgetTargetAction;
import org.csstudio.opibuilder.commands.AddWidgetCommand;
import org.csstudio.opibuilder.commands.OrphanChildCommand;
import org.csstudio.opibuilder.commands.WidgetDeleteCommand;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.editparts.GroupingContainerEditPart;
import org.csstudio.opibuilder.widgets.model.GroupingContainerModel;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.action.IAction;

/**
 * The action will remove a group and move all the selected widgets to the group's parent.
 */
public class RemoveGroupAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {
        var compoundCommand = new CompoundCommand("Remove Group");

        var containerModel = getSelectedContainer();

        // Orphan order should be reversed so that undo operation has the correct order.
        var widgetsArray = containerModel.getChildren()
                .toArray(new AbstractWidgetModel[containerModel.getChildren().size()]);
        for (var i = widgetsArray.length - 1; i >= 0; i--) {
            compoundCommand.add(new OrphanChildCommand(containerModel, widgetsArray[i]));
        }

        var leftCorner = containerModel.getLocation();
        for (AbstractWidgetModel widget : containerModel.getChildren()) {
            compoundCommand.add(new AddWidgetCommand(containerModel.getParent(), widget,
                    new Rectangle(widget.getLocation(), widget.getSize()).translate(leftCorner)));
        }
        compoundCommand.add(new WidgetDeleteCommand(containerModel.getParent(), containerModel));
        execute(compoundCommand);

    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final GroupingContainerModel getSelectedContainer() {
        return ((GroupingContainerEditPart) selection.getFirstElement()).getWidgetModel();
    }

}
