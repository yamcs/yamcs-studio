/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.dnd;

import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;

/**
 * The editpolicy for dropping pv onto a PV widget.
 */
public class DropPVtoPVWidgetEditPolicy extends AbstractEditPolicy {

    public final static String DROP_PV_ROLE = "DropPVEditPolicy";

    @Override
    public Command getCommand(Request request) {
        if (request.getType() == DropPVRequest.REQ_DROP_PV && request instanceof DropPVRequest) {
            var dropPVRequest = (DropPVRequest) request;
            if (dropPVRequest.getTargetWidget() != null) {
                return new SetWidgetPropertyCommand(dropPVRequest.getTargetWidget().getWidgetModel(),
                        AbstractPVWidgetModel.PROP_PVNAME, dropPVRequest.getPvNames()[0]);
            }
        }
        return super.getCommand(request);
    }

    @Override
    public EditPart getTargetEditPart(Request request) {
        if (request.getType() == DropPVRequest.REQ_DROP_PV) {
            return getHost();
        }
        return super.getTargetEditPart(request);
    }
}
