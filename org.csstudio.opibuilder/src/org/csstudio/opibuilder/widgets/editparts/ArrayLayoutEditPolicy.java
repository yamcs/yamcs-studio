/********************************************************************************
 * Copyright (c) 2012, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.editparts;

import java.util.List;

import org.csstudio.opibuilder.commands.AddWidgetCommand;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.commands.WidgetCreateCommand;
import org.csstudio.opibuilder.commands.WidgetSetConstraintCommand;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editpolicies.WidgetXYLayoutEditPolicy;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.model.ArrayModel;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

/**
 * The EditPolicy for array widget. It can only be used for {@link ArrayEditPart}
 */
public class ArrayLayoutEditPolicy extends WidgetXYLayoutEditPolicy {

    @Override
    protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
        if (request.getType().equals(REQ_MOVE_CHILDREN) || request.getType().equals(REQ_ALIGN_CHILDREN)) {
            return null;
        }
        return super.createChangeConstraintCommand(request, child, constraint);

    }

    @Override
    protected Command createAddCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
        if (!(child instanceof AbstractBaseEditPart) || !(constraint instanceof Rectangle)) {
            return super.createAddCommand(request, child, constraint);
        }

        var container = (AbstractContainerModel) getHost().getModel();
        if (!container.getChildren().isEmpty()) {
            return null;
        }
        var widget = (AbstractWidgetModel) child.getModel();
        var result = new CompoundCommand("Add widget to array");
        addUpdateContainerCommands(container, widget.getSize(), result);
        result.add(new AddWidgetCommand(container, widget, (Rectangle) constraint));

        return result;
    }

    protected void addUpdateContainerCommands(AbstractContainerModel container, Dimension widgetSize,
            CompoundCommand result) {
        var elementsCount = getHostArrayEditPart().getArrayFigure().calcVisibleElementsCount(widgetSize);
        var proposedContainerSize = getHostArrayEditPart().getArrayFigure().calcWidgetSizeForElements(elementsCount,
                widgetSize);
        result.add(new WidgetSetConstraintCommand(container, null,
                new Rectangle(container.getLocation(), proposedContainerSize)));
        result.add(new SetWidgetPropertyCommand(container, ArrayModel.PROP_VISIBLE_ELEMENTS_COUNT, elementsCount));
    }

    public ArrayEditPart getHostArrayEditPart() {
        return (ArrayEditPart) getHost();
    }

    @Override
    protected Command createWidgetCreateCommand(CreateRequest request) {
        var container = (AbstractContainerModel) getHost().getModel();
        if (!container.getChildren().isEmpty()) {
            return null;
        }
        var result = new CompoundCommand("Create widget in array");
        var size = ((Rectangle) getConstraintFor(request)).getSize();
        var widget = (AbstractWidgetModel) request.getNewObject();
        if (size == null || size.width < 1 || size.height < 1) {
            size = widget.getSize();
        }
        addUpdateContainerCommands(container, size, result);
        var widgetCreateCommand = new WidgetCreateCommand(widget, container, (Rectangle) getConstraintFor(request),
                false, true);
        result.add(widgetCreateCommand);
        return result;
    }

    /**
     * The behavior of resizing children in an array will be determined by its editpart.
     *
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#
     *      getResizeChildrenCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
     */
    @Override
    protected Command getResizeChildrenCommand(ChangeBoundsRequest request) {
        if (request.getType().equals(REQ_MOVE_CHILDREN) || request.getType().equals(REQ_ALIGN_CHILDREN)) {
            return null;
        }
        var resize = new CompoundCommand();
        Command c;
        List<?> children = getHostArrayEditPart().getChildren();
        var child = (GraphicalEditPart) request.getEditParts().get(0);
        var contraint = translateToModelConstraint(getConstraintForResize(request, child));
        c = createChangeConstraintCommand(request, (EditPart) children.get(0), contraint);
        resize.add(c);

        return resize.unwrap();
    }

    /* Override super method because array widget only allows adding one child.
     */
    @Override
    protected Command getAddCommand(Request generic) {
        var request = (ChangeBoundsRequest) generic;
        List<?> editParts = request.getEditParts();
        var command = new CompoundCommand();
        command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");
        GraphicalEditPart child;
        if (editParts.size() > 0) {
            child = (GraphicalEditPart) editParts.get(0);
            command.add(createAddCommand(request, child, translateToModelConstraint(getConstraintFor(request, child))));
        }
        return command.unwrap();
    }

}
