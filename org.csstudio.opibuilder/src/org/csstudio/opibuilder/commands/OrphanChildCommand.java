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
import org.eclipse.gef.commands.Command;

/**
 * Orphan a child from its parent.
 */
public class OrphanChildCommand extends Command {

    private AbstractContainerModel parent;
    private AbstractWidgetModel child;

    private int index;

    public OrphanChildCommand(AbstractContainerModel parent, AbstractWidgetModel child) {
        super("Orphan Widget");
        this.parent = parent;
        this.child = child;
    }

    @Override
    public void execute() {
        index = parent.getIndexOf(child);
        parent.removeChild(child);
    }

    @Override
    public void undo() {
        parent.addChild(index, child);
    }

}
