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
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;

/**
 * The action will auto size the selected widget to fill its parent container.
 */
public class FillParentContainerAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {

        var widget = (AbstractBaseEditPart) selection.getFirstElement();

        var containerEditpart = getParentContainerEditpart();

        Dimension size = null;
        if (containerEditpart instanceof DisplayEditpart) {
            size = ((DisplayEditpart) containerEditpart).getWidgetModel().getSize();
        } else {
            size = containerEditpart.getFigure().getClientArea().getSize();
        }

        Command cmd = new SetBoundsCommand(widget.getWidgetModel(), new Rectangle(0, 0, size.width, size.height));

        execute(cmd);
    }

    protected final AbstractContainerEditpart getParentContainerEditpart() {
        return (AbstractContainerEditpart) ((AbstractBaseEditPart) selection.getFirstElement()).getParent();
    }
}
