/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.commands;

import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.commands.Command;

/**
 * The command to change the order of a child.
 */
public class ChangeOrderCommand extends Command {

    private int newIndex;

    private int oldIndex;

    private AbstractContainerModel container;

    private AbstractWidgetModel widget;

    public ChangeOrderCommand(int newIndex, AbstractContainerModel container, AbstractWidgetModel widget) {
        Assert.isNotNull(container);
        Assert.isNotNull(widget);
        this.newIndex = newIndex;
        this.container = container;
        this.widget = widget;
    }

    @Override
    public boolean canExecute() {
        return newIndex != container.getIndexOf(widget);
    }

    @Override
    public void execute() {
        oldIndex = container.getIndexOf(widget);
        container.changeChildOrder(widget, newIndex);
        container.selectWidget(widget, true);
    }

    @Override
    public void undo() {
        container.changeChildOrder(widget, oldIndex);
    }

}
