/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.editparts.FixedPointsConnectionRouter;
import org.csstudio.opibuilder.editparts.WidgetConnectionEditPart;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * The action to reroute a connection.
 */
public class RerouteConnectionAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {
        execute(new SetWidgetPropertyCommand(getSelectedConnection().getWidgetModel(), ConnectionModel.PROP_POINTS,
                new PointList()));
    }

    protected WidgetConnectionEditPart getSelectedConnection() {
        return (WidgetConnectionEditPart) getSelection().getFirstElement();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (getSelectedConnection() == null) {
            return;
        }
        Connection figure = getSelectedConnection().getConnectionFigure();
        action.setEnabled(figure != null && figure.getConnectionRouter() instanceof FixedPointsConnectionRouter);
    }
}
