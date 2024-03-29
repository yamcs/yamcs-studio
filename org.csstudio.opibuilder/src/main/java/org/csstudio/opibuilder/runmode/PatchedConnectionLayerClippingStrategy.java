/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.ViewportAwareConnectionLayerClippingStrategy;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A patched connection layer clipping strategy that will hide the connection when either one of the source or target is
 * not showing.
 */
public class PatchedConnectionLayerClippingStrategy extends ViewportAwareConnectionLayerClippingStrategy {

    public PatchedConnectionLayerClippingStrategy(ConnectionLayer connectionLayer) {
        super(connectionLayer);
    }

    @Override
    protected Rectangle[] getEdgeClippingRectangle(Connection connection) {
        // start with clipping the connection at its original bounds
        var clipRect = getAbsoluteBoundsAsCopy(connection);

        // in case we cannot infer source and target of the connection (e.g.
        // if XYAnchors are used), returning the bounds is all we can do
        var sourceAnchor = connection.getSourceAnchor();
        var targetAnchor = connection.getTargetAnchor();
        if (sourceAnchor == null || sourceAnchor.getOwner() == null || targetAnchor == null
                || targetAnchor.getOwner() == null) {
            return new Rectangle[] { clipRect };
        }
        // source and target figure are known, see if there is common
        // viewport
        // the connection has to be clipped at.
        var sourceFigure = sourceAnchor.getOwner();
        var targetFigure = targetAnchor.getOwner();
        if (!sourceFigure.isShowing() || !targetFigure.isShowing()) {
            return new Rectangle[] {};
        }
        return super.getEdgeClippingRectangle(connection);
    }
}
