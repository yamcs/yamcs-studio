/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

/**
 * This class can be used to manimuplate points.
 */
public final class PointsUtil {

    /**
     * Private constructor, to avoid instantiation.
     */
    private PointsUtil() {
    }

    /**
     * Rotates the given {@link Point} with the given angle relative to the rotation point. Converts the given point to
     * a {@link PrecisionPoint} and calls {@link #doRotate(PrecisionPoint, double, PrecisionPoint)}.
     * 
     * @param point
     *            The {@link Point} to rotate
     * @param angle
     *            The angle to rotate (in Degrees)
     * @param rotationPoint
     *            The rotation point
     * @return The rotated Point
     */
    public static PrecisionPoint rotate(Point point, double angle, Point rotationPoint) {
        var pPoint = point instanceof PrecisionPoint ? (PrecisionPoint) point : new PrecisionPoint(point);
        var pRotationPoint = rotationPoint instanceof PrecisionPoint ? (PrecisionPoint) rotationPoint
                : new PrecisionPoint(rotationPoint);

        return doRotate(pPoint, angle, pRotationPoint);
    }

    /**
     * Rotates the given {@link Point} with the given angle relative to the rotation point.
     * 
     * @param point
     *            The {@link Point} to rotate
     * @param angle
     *            The angle to rotate (in Degrees)
     * @param rotationPoint
     *            The rotation point
     * @return The rotated Point
     */
    public static PrecisionPoint doRotate(PrecisionPoint point, double angle, PrecisionPoint rotationPoint) {
        assert point != null : "Precondition violated: point!=null";
        assert rotationPoint != null : "Precondition violated: rotationPoint!=null";
        var trueAngle = Math.toRadians(angle);
        var sin = Math.sin(trueAngle);
        var cos = Math.cos(trueAngle);

        // Relative coordinates to the rotation point
        var relX = point.preciseX - rotationPoint.preciseX;
        var relY = point.preciseY - rotationPoint.preciseY;

        var temp = relX * cos - relY * sin;

        var y = relX * sin + relY * cos;
        var x = temp;

        return new PrecisionPoint(x + rotationPoint.x, y + rotationPoint.y);
    }

    /**
     * Rotates all points.
     *
     * @param points
     *            The PoinList, which points should be rotated
     * @param angle
     *            The angle to rotate
     * @return The rotated PointList
     */
    public static PointList rotatePoints(PointList points, double angle) {
        var pointBounds = points.getBounds();
        var rotationPoint = pointBounds.getCenter();
        var newPoints = rotatePoints(points, angle, rotationPoint);
        var newPointBounds = newPoints.getBounds();
        if (!rotationPoint.equals(newPointBounds.getCenter())) {
            var difference = rotationPoint.getCopy().getDifference(newPointBounds.getCenter());
            newPoints.translate(difference.width, difference.height);
        }
        return newPoints;
    }

    /**
     * Rotates all points.
     *
     * @param points
     *            The PoinList, which points should be rotated
     * @param angle
     *            The angle to rotate
     * @return The rotated PointList
     */
    public static PointList rotatePoints(PointList points, double angle, Point center) {
        var newPoints = new PointList();

        for (var i = 0; i < points.size(); i++) {
            newPoints.addPoint(PointsUtil.rotate(points.getPoint(i), angle, center));
        }

        return newPoints;
    }

    /**
     * Flip point horizontally from center point.
     * 
     * @param point
     *            the point to be flipped.
     * @param center
     *            the center point.
     * @return the point after flipping.
     */
    public static Point flipPointHorizontally(Point point, int center) {
        var newX = 2 * center - point.x;
        return new Point(newX, point.y);
    }

    /**
     * Flip point vertically from center point.
     * 
     * @param point
     *            the point to be flipped.
     * @param center
     *            the center point.
     * @return the point after flipping.
     */
    public static Point flipPointVertically(Point point, int center) {
        var newY = 2 * center - point.y;
        return new Point(point.x, newY);
    }

    /**
     * Flip points horizontally.
     * 
     * @param points
     *            the points to be flipped.
     * @return the flipped points.
     */
    public static PointList flipPointsHorizontally(PointList points) {

        var centerX = points.getBounds().x + points.getBounds().width / 2;

        return flipPointsHorizontally(points, centerX);
    }

    /**
     * Flip points horizontally.
     * 
     * @param points
     *            the points to be flipped.
     * @param centerX
     *            the center X position
     * @return the flipped points.
     */
    public static PointList flipPointsHorizontally(PointList points, int centerX) {

        var newPointList = new PointList();

        for (var i = 0; i < points.size(); i++) {
            newPointList.addPoint(flipPointHorizontally(points.getPoint(i), centerX));
        }

        return newPointList;
    }

    /**
     * Flip points vertically.
     * 
     * @param points
     *            the points to be flipped.
     * @return the flipped points.
     */
    public static PointList flipPointsVertically(PointList points) {

        var centerY = points.getBounds().y + points.getBounds().height / 2;

        return flipPointsVertically(points, centerY);
    }

    /**
     * Flip points vertically.
     * 
     * @param points
     *            the points to be flipped.
     * @param centerY
     *            the center Y position.
     * @return the flipped points.
     */
    public static PointList flipPointsVertically(PointList points, int centerY) {

        var newPointList = new PointList();

        for (var i = 0; i < points.size(); i++) {
            newPointList.addPoint(flipPointVertically(points.getPoint(i), centerY));
        }

        return newPointList;
    }

    /**
     * Scale the geometry size of a pointlist.
     * 
     * @param points
     *            points to be scaled.
     * @param widthRatio
     *            width scale ratio.
     * @param heightRatio
     *            height scale ratio.
     */
    public static void scalePoints(PointList points, double widthRatio, double heightRatio) {
        var p0 = points.getBounds().getLocation();
        for (var i = 0; i < points.size(); i++) {
            var p = points.getPoint(i);
            p.x = (int) ((p.x - p0.x) * widthRatio) + p0.x;
            p.y = (int) ((p.y - p0.y) * heightRatio) + p0.y;
            points.setPoint(p, i);
        }
    }

    /**
     * Scale the bound size of a point list.
     * 
     * @param points
     *            the points to be scaled.
     * @param width
     *            the new width.
     * @param height
     *            the new height
     * @return the points after scaled. If no scale is needed, return the input points.
     */
    public static PointList scalePointsBySize(PointList points, int width, int height) {
        var targetW = Math.max(1, width);
        var targetH = Math.max(1, height);
        double oldW = points.getBounds().width;
        double oldH = points.getBounds().height;
        double topLeftX = points.getBounds().x;
        double topLeftY = points.getBounds().y;

        if (oldW != targetW || oldH != targetH) {
            var newPoints = new PointList();
            for (var i = 0; i < points.size(); i++) {
                var x = points.getPoint(i).x;
                var y = points.getPoint(i).y;

                var newPoint = new Point(x, y);
                if (oldW > 0 && oldH > 0) {
                    var oldRelX = (x - topLeftX) / oldW;
                    var oldRelY = (y - topLeftY) / oldH;

                    var newX = topLeftX + (oldRelX * targetW);
                    var newY = topLeftY + (oldRelY * targetH);
                    var roundedX = (int) Math.round(newX);
                    var roundedY = (int) Math.round(newY);
                    newPoint = new Point(roundedX, roundedY);
                }

                newPoints.addPoint(newPoint);
            }
            return newPoints;
        }
        return points;
    }
}
