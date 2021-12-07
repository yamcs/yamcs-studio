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

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * An edit policy that pass MOVE request to its parent.
 */
public class WidgetTreeEditPolicy extends AbstractEditPolicy {

    @Override
    public Command getCommand(Request req) {
        if (REQ_MOVE.equals(req.getType())) {
            return getMoveCommand((ChangeBoundsRequest) req);
        }
        return null;
    }

    protected Command getMoveCommand(ChangeBoundsRequest req) {
        var parent = getHost().getParent();
        if (parent != null) {
            var request = new ChangeBoundsRequest(REQ_MOVE_CHILDREN);
            // request.setEditParts(getHost());
            request.setEditParts(req.getEditParts());
            request.setLocation(req.getLocation());
            req.setType("");
            return parent.getCommand(request);
        }
        return UnexecutableCommand.INSTANCE;
    }

}
