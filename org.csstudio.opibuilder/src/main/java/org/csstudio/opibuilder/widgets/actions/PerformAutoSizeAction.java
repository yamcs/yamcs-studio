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
import org.csstudio.opibuilder.commands.SetBoundsCommand;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.csstudio.opibuilder.util.GeometryUtil;
import org.csstudio.opibuilder.widgets.editparts.GroupingContainerEditPart;
import org.csstudio.opibuilder.widgets.model.GroupingContainerModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.action.IAction;

/**
 * The action will auto size the container according to the bounds of its children.
 */
public class PerformAutoSizeAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {
        if (getContainerEditpart().getChildren().size() <= 0) {
            return;
        }
        var compoundCommand = new CompoundCommand("Perform AutoSize");

        var containerEditpart = getContainerEditpart();
        var containerModel = containerEditpart.getWidgetModel();

        // temporary unlock children so children will not be resized.
        if (containerEditpart instanceof GroupingContainerEditPart) {
            compoundCommand.add(
                    new SetWidgetPropertyCommand(containerModel, GroupingContainerModel.PROP_LOCK_CHILDREN, false));
        }

        var figure = getContainerFigure();

        var childrenRange = GeometryUtil.getChildrenRange(containerEditpart);

        var tranlateSize = new Point(childrenRange.x, childrenRange.y);

        compoundCommand.add(new SetBoundsCommand(containerModel,
                new Rectangle(containerModel.getLocation().translate(tranlateSize),
                        new Dimension(childrenRange.width + figure.getInsets().left + figure.getInsets().right,
                                childrenRange.height + figure.getInsets().top + figure.getInsets().bottom))));

        for (var editpart : containerEditpart.getChildren()) {
            var widget = ((AbstractBaseEditPart) editpart).getWidgetModel();
            compoundCommand.add(new SetBoundsCommand(widget,
                    new Rectangle(widget.getLocation().translate(tranlateSize.getNegated()), widget.getSize())));
        }

        // recover lock
        if (containerEditpart instanceof GroupingContainerEditPart) {
            var oldvalue = containerEditpart.getWidgetModel()
                    .getPropertyValue(GroupingContainerModel.PROP_LOCK_CHILDREN);
            compoundCommand.add(
                    new SetWidgetPropertyCommand(containerModel, GroupingContainerModel.PROP_LOCK_CHILDREN, oldvalue));
        }

        execute(compoundCommand);
    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final AbstractContainerEditpart getContainerEditpart() {
        return (AbstractContainerEditpart) selection.getFirstElement();
    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final IFigure getContainerFigure() {
        return ((AbstractContainerEditpart) selection.getFirstElement()).getFigure();
    }
}
