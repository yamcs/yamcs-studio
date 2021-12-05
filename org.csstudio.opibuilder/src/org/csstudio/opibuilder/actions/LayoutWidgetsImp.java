/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.commands.SetBoundsCommand;
import org.csstudio.opibuilder.editparts.AbstractLayoutEditpart;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;

/**
 * The common code for {@link LayoutWidgetsAction} and {@link LayoutWidgetsHandler}.
 */
public class LayoutWidgetsImp {

    public static void run(AbstractLayoutEditpart layoutWidget, CommandStack commandStack) {

        AbstractContainerModel container = layoutWidget.getWidgetModel().getParent();

        List<AbstractWidgetModel> modelChildren = new ArrayList<AbstractWidgetModel>();
        modelChildren.addAll(container.getChildren());
        modelChildren.remove(layoutWidget.getWidgetModel());

        if (modelChildren.size() == 0)
            return;

        List<Rectangle> newBounds = layoutWidget.getNewBounds(modelChildren, container.getBounds());

        CompoundCommand compoundCommand = new CompoundCommand("Layout Widgets");

        int i = 0;
        for (AbstractWidgetModel model : modelChildren) {
            compoundCommand.add(new SetBoundsCommand(model, newBounds.get(i)));
            i++;
        }

        commandStack.execute(compoundCommand);

    }

}
