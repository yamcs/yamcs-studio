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
import org.csstudio.opibuilder.commands.WidgetCreateCommand;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.util.WidgetsService;
import org.csstudio.opibuilder.visualparts.WidgetsSelectDialog;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.window.Window;

/**
 * The editpolicy for dropping pv onto a PV widget.
 */
public class DropPVtoContainerEditPolicy extends DropPVtoPVWidgetEditPolicy {

    private static final int WIDGETS_ACCOUNT_ON_A_ROW = 5;

    @Override
    public Command getCommand(Request request) {
        if (request.getType() == DropPVRequest.REQ_DROP_PV && request instanceof DropPVRequest) {
            var dropPVRequest = (DropPVRequest) request;
            if (dropPVRequest.getTargetWidget() != null
                    && dropPVRequest.getTargetWidget() instanceof AbstractContainerEditpart) {
                var dialog = new WidgetsSelectDialog(getHost().getViewer().getControl().getShell(),
                        dropPVRequest.getPvNames().length, true);

                if (dialog.open() == Window.OK) {
                    var typeID = dialog.getOutput();
                    var command = new CompoundCommand("Create Widget");
                    var pvNames = dropPVRequest.getPvNames();
                    var location = dropPVRequest.getLocation().getCopy();
                    var container = ((AbstractContainerEditpart) dropPVRequest.getTargetWidget()).getWidgetModel();
                    var parent = container.getParent();
                    var temp = container;
                    while (parent != null) {
                        location.translate(temp.getLocation().getNegated());
                        temp = parent;
                        parent = parent.getParent();
                    }
                    var i = 1;
                    int lastWidth = 0, lastHeight = 0;
                    for (var pvName : pvNames) {
                        var widgetModel = WidgetsService.getInstance().getWidgetDescriptor(typeID).getWidgetModel();
                        command.add(new WidgetCreateCommand(widgetModel, container,
                                new Rectangle(location.getCopy().translate(lastWidth, lastHeight),
                                        new Dimension(-1, -1)),
                                i != 1, true));
                        command.add(new SetWidgetPropertyCommand(widgetModel, AbstractPVWidgetModel.PROP_PVNAME,
                                pvName.trim()));
                        if (i % WIDGETS_ACCOUNT_ON_A_ROW == 0) {
                            lastWidth = 0;
                            lastHeight += widgetModel.getHeight();
                        } else {
                            lastWidth += widgetModel.getWidth();
                        }
                        i++;
                    }

                    return command;
                }

            }
        }
        return null;
    }

    @Override
    public EditPart getTargetEditPart(Request request) {
        if (request.getType() == DropPVRequest.REQ_DROP_PV) {
            return getHost();
        }
        return super.getTargetEditPart(request);
    }
}
