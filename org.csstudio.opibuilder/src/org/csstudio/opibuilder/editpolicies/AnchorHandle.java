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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.handles.SquareHandle;
import org.eclipse.swt.graphics.Color;

/**
 * The handle that shows the small red square on anchor.
 */
public class AnchorHandle extends SquareHandle {

    public AnchorHandle(GraphicalEditPart owner, ConnectionAnchor anchor) {

        setOwner(owner);
        setLocator(new Locator() {
            @Override
            public void relocate(IFigure target) {
                var center = anchor.getLocation(null);
                target.translateToRelative(center);
                target.setBounds(new Rectangle(center.x - DEFAULT_HANDLE_SIZE / 2, center.y - DEFAULT_HANDLE_SIZE / 2,
                        DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE));
            }
        });
    }

    @Override
    protected DragTracker createDragTracker() {
        return null;
    }

    @Override
    protected Color getBorderColor() {
        return ColorConstants.darkGray;
    }

    @Override
    protected Color getFillColor() {
        return ColorConstants.red;
    }
}
