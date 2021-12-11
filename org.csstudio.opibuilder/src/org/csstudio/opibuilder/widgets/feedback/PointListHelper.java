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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A transformator utility for {@link PointList} objects.
 */
public final class PointListHelper {

    /**
     * Private constructor to prevent instantiation.
     *
     */
    private PointListHelper() {

    }

    /**
     * Transforms the points in the specified point list to fit the given size. All point coordinates are transformed
     * relatively to the new size.
     *
     * @param points
     *            the point list
     * @param width
     *            the new width
     * @param height
     *            the new height
     * @return a point list copy, which has been scaled to the new size
     */
    public static PointList scaleToSize(PointList points, int width, int height) {
        // assert points != null;
        if (width <= 0 || height <= 0) {
            return points;
            // throw new IllegalArgumentException(
            // "Illegal dimensions. Width and height must be > 0.");
        }

        double oldW = points.getBounds().width;
        double oldH = points.getBounds().height;
        double topLeftX = points.getBounds().x;
        double topLeftY = points.getBounds().y;

        var newPoints = new PointList();

        for (var i = 0; i < points.size(); i++) {
            var x = points.getPoint(i).x;
            var y = points.getPoint(i).y;

            var newPoint = new Point(x, y);
            if (oldW != 0 && oldH != 0) {
                var oldRelX = (x - topLeftX) / oldW;
                var oldRelY = (y - topLeftY) / oldH;

                var newX = topLeftX + (oldRelX * width);
                var newY = topLeftY + (oldRelY * height);
                newPoint = new Point((int) newX, (int) newY);
            }

            newPoints.addPoint(newPoint);
        }

        return newPoints;
    }

    /**
     * Moves the origin (0,0) of the coordinate system of all the points in the specified point list to the Point (x,y).
     *
     * @param points
     *            the point list
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @return a point list copy, which has been scaled to the new location
     */
    public static PointList scaleToLocation(PointList points, int x, int y) {
        var oldX = points.getBounds().x;
        var oldY = points.getBounds().y;

        var result = points.getCopy();
        result.translate(x - oldX, y - oldY);
        return result;
    }

    /**
     * Scales the point list to the new bounds.
     *
     * @param points
     *            the point list
     * @param targetBounds
     *            the target bounds
     * @return a point list copy, which has been scaled to the new bounds
     */
    public static PointList scaleTo(PointList points, Rectangle targetBounds) {
        var result = scaleToLocation(points, targetBounds.x, targetBounds.y);
        result = scaleToSize(result, targetBounds.width, targetBounds.height);

        return result;

    }
}
