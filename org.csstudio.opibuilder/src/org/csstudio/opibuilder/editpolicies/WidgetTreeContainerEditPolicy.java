/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editpolicies;

import org.csstudio.opibuilder.commands.ChangeOrderCommand;
import org.csstudio.opibuilder.commands.WidgetCreateCommand;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

/**
 * The edit policy for widgets operation on a tree.
 */
public class WidgetTreeContainerEditPolicy extends TreeContainerEditPolicy {

    @Override
    protected Command getAddCommand(ChangeBoundsRequest request) {
        var cmd = new CompoundCommand();
        var editparts = request.getEditParts();
        var index = findIndexOfTreeItemAt(request.getLocation());
        for (var i = 0; i < editparts.size(); i++) {
            var child = (EditPart) editparts.get(index >= 0 ? editparts.size() - 1 - i : i);
            if (isAncestor(child, getHost())) {
                cmd.add(UnexecutableCommand.INSTANCE);
            } else {
                var childModel = (AbstractWidgetModel) child.getModel();
                cmd.add(createCreateCommand(childModel, new Rectangle(new Point(), childModel.getSize()), index,
                        "Reparent Widgets"));
            }
        }
        return cmd;
    }

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        var widgetModel = (AbstractWidgetModel) request.getNewObject();
        var index = findIndexOfTreeItemAt(request.getLocation());
        return createCreateCommand(widgetModel, null, index, "Create Widget");
    }

    @Override
    protected Command getMoveChildrenCommand(ChangeBoundsRequest request) {
        var command = new CompoundCommand();
        var editparts = request.getEditParts();
        var children = getHost().getChildren();
        var newIndex = findIndexOfTreeItemAt(request.getLocation());
        var tempIndex = newIndex;

        for (var i = 0; i < editparts.size(); i++) {
            var child = (EditPart) editparts.get(editparts.size() - 1 - i);

            var oldIndex = children.indexOf(child);
            if (oldIndex == tempIndex || oldIndex + 1 == tempIndex) {
                command.add(UnexecutableCommand.INSTANCE);
                return command;
            } else if (oldIndex <= tempIndex) {
                tempIndex--;
            }

            command.add(new ChangeOrderCommand(tempIndex, (AbstractContainerModel) getHost().getModel(),
                    (AbstractWidgetModel) child.getModel()));
        }
        return command;
    }

    protected Command createCreateCommand(AbstractWidgetModel widgetModel, Rectangle r, int index, String label) {
        var cmd = new WidgetCreateCommand(widgetModel, (AbstractContainerModel) getHost().getModel(), r, false, true);
        cmd.setLabel(label);
        cmd.setIndex(index);
        return cmd;

    }

    protected boolean isAncestor(EditPart source, EditPart target) {
        if (source == target) {
            return true;
        }
        if (target.getParent() != null) {
            return isAncestor(source, target.getParent());
        }
        return false;
    }
}
