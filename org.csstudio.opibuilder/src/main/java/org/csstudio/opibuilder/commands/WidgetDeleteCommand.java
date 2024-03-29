/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.commands;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.eclipse.gef.commands.Command;

/**
 * The command to delete a widget.
 */
public class WidgetDeleteCommand extends Command {

    private final AbstractContainerModel container;

    private int index;

    private final AbstractWidgetModel widget;

    private List<ConnectionModel> sourceConnections, targetConnections;

    public WidgetDeleteCommand(AbstractContainerModel container, AbstractWidgetModel widget) {
        assert container != null;
        assert widget != null;
        this.container = container;
        this.widget = widget;
    }

    @Override
    public void execute() {
        sourceConnections = getAllConnections(widget, true);
        targetConnections = getAllConnections(widget, false);
        redo();
    }

    private List<ConnectionModel> getAllConnections(AbstractWidgetModel widget, boolean source) {
        List<ConnectionModel> result = new ArrayList<>();
        result.addAll(source ? widget.getSourceConnections() : widget.getTargetConnections());
        if (widget instanceof AbstractContainerModel) {
            for (var child : ((AbstractContainerModel) widget).getAllDescendants()) {
                result.addAll(source ? child.getSourceConnections() : child.getTargetConnections());
            }
        }
        return result;
    }

    @Override
    public void redo() {
        index = container.getIndexOf(widget);
        container.removeChild(widget);
        removeConnections(sourceConnections);
        removeConnections(targetConnections);
    }

    @Override
    public void undo() {
        container.addChild(index, widget);
        addConnections(sourceConnections);
        addConnections(targetConnections);
    }

    private void removeConnections(List<ConnectionModel> connections) {
        for (var conn : connections) {
            conn.disconnect();
        }
    }

    private void addConnections(List<ConnectionModel> connections) {
        for (var conn : connections) {
            conn.reconnect();
        }
    }
}
