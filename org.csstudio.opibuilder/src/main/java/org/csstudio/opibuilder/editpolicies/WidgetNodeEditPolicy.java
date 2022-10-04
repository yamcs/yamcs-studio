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

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.commands.ConnectionCreateCommand;
import org.csstudio.opibuilder.commands.ConnectionReconnectCommand;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.model.ConnectionModel.RouterType;
import org.csstudio.opibuilder.util.SchemaService;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * The editpolicy that allows the creation of connections and the reconnection of connections between widgets.
 */
public class WidgetNodeEditPolicy extends GraphicalNodeEditPolicy {

    static {
        // Loading a schema service for the first time when the first widget connection is
        // created throws an exception, because the schema service is loaded in the UI thread.
        //
        // Opening it in the UI thread creates a modal progress dialog, and this interferes
        // with connection which is drawn from the first anchor to the mouse pointer.
        //
        // To avoid this situation, we make sure that the schema service is already loaded
        // when we add the first connector. The schema service is a singleton and will
        // just be returned whenever requested from now on.
        SchemaService.getInstance();
    }

    /**
     * the List of handles
     */
    protected List<AnchorHandle> handles;

    @Override
    protected ConnectionRouter getDummyConnectionRouter(CreateConnectionRequest request) {
        int i = (Integer) SchemaService.getInstance().getDefaultPropertyValue(ConnectionModel.ID,
                ConnectionModel.PROP_ROUTER);
        var routerType = RouterType.values()[i];
        switch (routerType) {
        case MANHATTAN:
            return new ManhattanConnectionRouter();
        case STRAIGHT_LINE:
        default:
            return super.getDummyConnectionRouter(request);
        }
    }

    @Override
    protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
        var cmd = (ConnectionCreateCommand) request.getStartCommand();
        cmd.setTarget(getWidgetEditPart().getWidgetModel());
        var anchor = getWidgetEditPart().getTargetConnectionAnchor(request);
        if (anchor == null) {
            return null;
        }
        cmd.setTargetTerminal(getWidgetEditPart().getTerminalNameFromAnchor(anchor));
        return cmd;
    }

    @Override
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        var source = getWidgetEditPart().getWidgetModel();
        var anchor = getWidgetEditPart().getSourceConnectionAnchor(request);
        var sourceTerminal = getWidgetEditPart().getTerminalNameFromAnchor(anchor);
        var cmd = new ConnectionCreateCommand(source, sourceTerminal);
        request.setStartCommand(cmd);
        return cmd;
    }

    @Override
    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        var connection = (ConnectionModel) request.getConnectionEditPart().getModel();
        var newTarget = getWidgetEditPart().getWidgetModel();
        var anchor = getWidgetEditPart().getTargetConnectionAnchor(request);
        var newTerminal = getWidgetEditPart().getTerminalNameFromAnchor(anchor);
        var cmd = new ConnectionReconnectCommand(connection);
        cmd.setNewTarget(newTarget);
        cmd.setNewTargetTerminal(newTerminal);
        // clear point list
        return cmd.chain(new SetWidgetPropertyCommand(connection, ConnectionModel.PROP_POINTS, new PointList()));
    }

    @Override
    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        var connection = (ConnectionModel) request.getConnectionEditPart().getModel();
        var newSource = getWidgetEditPart().getWidgetModel();
        var anchor = getWidgetEditPart().getTargetConnectionAnchor(request);
        var newTerminal = getWidgetEditPart().getTerminalNameFromAnchor(anchor);
        var cmd = new ConnectionReconnectCommand(connection);
        cmd.setNewSource(newSource);
        cmd.setNewSourceTerminal(newTerminal);
        // clear point list
        return cmd.chain(new SetWidgetPropertyCommand(connection, ConnectionModel.PROP_POINTS, new PointList()));
    }

    protected AbstractBaseEditPart getWidgetEditPart() {
        return (AbstractBaseEditPart) getHost();
    }

    @Override
    protected void showTargetConnectionFeedback(DropRequest request) {
        addAnchorHandles();
    }

    @Override
    protected void eraseTargetConnectionFeedback(DropRequest request) {
        removeAnchorHandles();
    }

    /**
     * Adds the handles to the handle layer.
     */
    protected void addAnchorHandles() {
        removeAnchorHandles();
        var layer = getLayer(LayerConstants.HANDLE_LAYER);
        handles = createAnchorHandles();
        for (var i = 0; i < handles.size(); i++) {
            layer.add(handles.get(i));
        }
    }

    /**
     * create the list of handles.
     *
     * @return List of handles; cannot be <code>null</code>
     */
    protected List<AnchorHandle> createAnchorHandles() {
        var result = new ArrayList<AnchorHandle>();
        for (var anchor : getWidgetEditPart().getAnchorMap().values()) {
            result.add(new AnchorHandle(getWidgetEditPart(), anchor));
        }
        return result;
    }

    /**
     * removes the anchor handles
     */
    protected void removeAnchorHandles() {
        if (handles == null) {
            return;
        }
        var layer = getLayer(LayerConstants.HANDLE_LAYER);
        for (var i = 0; i < handles.size(); i++) {
            layer.remove(handles.get(i));
        }
        handles = null;
    }
}
