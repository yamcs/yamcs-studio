/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.dnd.DropPVRequest;
import org.csstudio.opibuilder.widgets.model.XYGraphModel;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;

/**
 * The editpolicy for dropping pv onto a XYGraph widget. It will add the dropped PVs to XYGraph as traces.
 */
public class DropPVtoXYGraphEditPolicy extends AbstractEditPolicy {

    public final static String DROP_PV_ROLE = "DropPVEditPolicy";

    @Override
    public Command getCommand(Request request) {
        if (request.getType() == DropPVRequest.REQ_DROP_PV && request instanceof DropPVRequest) {
            var dropPVRequest = (DropPVRequest) request;
            if (dropPVRequest.getTargetWidget() != null && dropPVRequest.getTargetWidget() instanceof XYGraphEditPart) {
                var command = new CompoundCommand("Add Traces");
                var xyGraphModel = (XYGraphModel) dropPVRequest.getTargetWidget().getWidgetModel();
                var existTraces = xyGraphModel.getTracesAmount();
                if (existTraces >= XYGraphModel.MAX_TRACES_AMOUNT) {
                    return null;
                }
                command.add(new SetWidgetPropertyCommand(xyGraphModel, XYGraphModel.PROP_TRACE_COUNT,
                        dropPVRequest.getPvNames().length + existTraces));
                var i = existTraces;
                for (String pvName : dropPVRequest.getPvNames()) {
                    command.add(new SetWidgetPropertyCommand(xyGraphModel,
                            XYGraphModel.makeTracePropID(XYGraphModel.TraceProperty.YPV.propIDPre, i), pvName));
                    command.add(new SetWidgetPropertyCommand(xyGraphModel,
                            XYGraphModel.makeTracePropID(XYGraphModel.TraceProperty.NAME.propIDPre, i), pvName));
                    if (++i >= XYGraphModel.MAX_TRACES_AMOUNT) {
                        break;
                    }
                }
                return command;
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
