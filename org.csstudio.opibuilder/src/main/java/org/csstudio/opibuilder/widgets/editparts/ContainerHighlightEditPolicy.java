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

import org.csstudio.opibuilder.dnd.DropPVRequest;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.swt.graphics.Color;

/**
 * The EditPolicy help to show the blue background when there are selected widgets hovering over the container.
 */
public class ContainerHighlightEditPolicy extends GraphicalEditPolicy {

    private static Color BACK_BLUE = CustomMediaFactory.getInstance().getColor(200, 200, 240);

    private Color revertColor;
    private boolean revertOpaque;

    @Override
    public EditPart getTargetEditPart(Request request) {
        return request.getType().equals(RequestConstants.REQ_SELECTION_HOVER) ? getHost() : null;
    }

    private Color getContainerBackground() {
        return getContainerFigure().getBackgroundColor();
    }

    private IFigure getContainerFigure() {
        return ((GraphicalEditPart) getHost()).getContentPane();
    }

    private void setContainerBackground(Color c) {
        getContainerFigure().setBackgroundColor(c);
    }

    protected void showHighlight() {
        if (revertColor == null) {
            revertColor = getContainerBackground();
            revertOpaque = getContainerFigure().isOpaque();
            setContainerBackground(BACK_BLUE);
            getContainerFigure().setOpaque(true);
        }
    }

    @Override
    public void eraseTargetFeedback(Request request) {
        if (revertColor != null) {
            setContainerBackground(revertColor);
            getContainerFigure().setOpaque(revertOpaque);
            revertColor = null;
        }
    }

    @Override
    public void showTargetFeedback(Request request) {
        if (request.getType().equals(RequestConstants.REQ_MOVE) || request.getType().equals(RequestConstants.REQ_ADD)
                || request.getType().equals(RequestConstants.REQ_CLONE)
                || request.getType().equals(RequestConstants.REQ_CREATE)
                || request.getType().equals(DropPVRequest.REQ_DROP_PV)) {
            showHighlight();
        }
    }
}
