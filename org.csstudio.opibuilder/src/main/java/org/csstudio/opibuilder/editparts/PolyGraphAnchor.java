/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editparts;

import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;

/**
 * This class represents an anchor on a polygon or polyline widget.
 * <p>
 * On a normal widget the there are just 8 possible positions for the connector, and they are all on the widget bounding
 * box:
 * <ul>
 * <li>4 are on the middle of each edge of the bounding box</li>
 * <li>4 are on each corner of the bounding box</li>
 * </ul>
 * This is true even if the widget does not will its bounding box completely.
 * <p>
 * On a polyline or a polygon widget the anchors are wherever there is a bend in the polyline.
 */
public class PolyGraphAnchor extends AbstractOpiBuilderAnchor {
    private int pointIndex;
    private Polyline polyline;

    public PolyGraphAnchor(Polyline owner, int pointIndex) {
        super(owner);
        polyline = owner;
        this.pointIndex = pointIndex;
    }

    @Override
    public Point getLocation(Point reference) {
        var p = polyline.getPoints().getPoint(pointIndex);
        polyline.translateToAbsolute(p);
        fixZoomEdgeRounding(p, getOwner());
        return p;
    }

    @Override
    public Point getReferencePoint() {
        return getLocation(null);
    }

    @Override
    public ConnectorOrientation getOrientation() {
        return getOrientation(polyline.getPoints().getPoint(pointIndex));
    }

    private ConnectorOrientation getOrientation(Point anchor) {
        // calculate the direction. The direction for now is decided like this:
        // if the connector is closest to the left or right side of the bounding box, then horizontal connection line is
        // selected
        // if the connector is closest to the top or bottom side of the bounding box, then vertical connection line is
        // selected
        var bounds = getOwner().getBounds();
        // The bounds in is relative coordinates, so relative to the clipping/bounding box if in a linked container ==>
        // translate the anchor as well
        var translatedAnchor = anchor.getCopy();
        // calculate the smallest absolute offset from the left and right bound
        var leftRight = Math.min(Math.abs(translatedAnchor.x - bounds.x),
                Math.abs(translatedAnchor.x - (bounds.x + bounds.width)));
        // calculate the smallest absolute offset from the top and bottom bound
        var topBottom = Math.min(Math.abs(translatedAnchor.y - bounds.y),
                Math.abs(translatedAnchor.y - (bounds.y + bounds.height)));
        // anchorPoint and midPoint are both in original coordinates
        if (leftRight < topBottom) {
            return ConnectorOrientation.HORIZONTAL;
        } else {
            return ConnectorOrientation.VERTICAL;
        }
    }
}
