/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Polyline;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.handles.SquareHandle;
import org.eclipse.swt.graphics.Color;

/**
 * A handle, used to move points of a polyline or polygon.
 *
 */
public final class PolyPointHandle extends SquareHandle {
    /**
     * Index of the polygon point, that should be moved.
     */
    private int _pointIndex;

    /**
     * Creates a new Handle for the given GraphicalEditPart.
     *
     * @param owner
     *            owner of the ResizeHandle
     * @param pointIndex
     *            index of the polygon point, that should be moved
     */
    public PolyPointHandle(GraphicalEditPart owner, int pointIndex) {
        _pointIndex = pointIndex;
        setOwner(owner);

        var locator = new PolyPointLocator((Polyline) owner.getFigure(), pointIndex);
        setLocator(locator);

        setCursor(Cursors.CROSS);
    }

    @Override
    protected DragTracker createDragTracker() {
        return new PolyPointDragTracker(getOwner(), _pointIndex);
    }

    @Override
    protected Color getBorderColor() {
        return ColorConstants.darkGray;
    }

    @Override
    protected Color getFillColor() {
        return ColorConstants.yellow;
    }
}
