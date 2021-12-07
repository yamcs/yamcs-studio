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

import org.csstudio.opibuilder.commands.OrphanChildCommand;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;

/**
 * Container edit policy which supports children orphan.
 */
public class WidgetContainerEditPolicy extends ContainerEditPolicy {

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        return null;
    }

    @Override
    protected Command getOrphanChildrenCommand(GroupRequest request) {
        var parts = request.getEditParts();
        var result = new CompoundCommand("Orphan Children");
        for (var i = 0; i < parts.size(); i++) {
            var orphan = new OrphanChildCommand((AbstractContainerModel) (getHost().getModel()),
                    (AbstractWidgetModel) ((EditPart) parts.get(i)).getModel());
            orphan.setLabel("Reparenting widget");
            result.add(orphan);
        }

        return result.unwrap();
    }

}
