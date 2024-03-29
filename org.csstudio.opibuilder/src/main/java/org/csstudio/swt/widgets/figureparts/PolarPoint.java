/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figureparts;

import java.util.Objects;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A polar point in a standard polar coordinates system.
 */
public class PolarPoint {

    /**
     * The radial coordinate
     */
    public int r;

    /**
     * The angular coordinate in radians
     */
    public double theta;

    /**
     * @param r
     *            The radial coordinate
     * @param theta
     *            The angular coordinate in radians
     */
    public PolarPoint(int r, double theta) {
        this.r = r;
        this.theta = theta;
    }

    // @Override
    // public boolean equals(Object obj) {
    // if(obj instanceof PolarPoint) {
    // PolarPoint p = (PolarPoint)obj;
    // return p.r == r && p.theta == theta;
    // }
    // return false;
    // }

    @Override
    public int hashCode() {
        return Objects.hash(r, theta);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (PolarPoint) obj;
        if (r != other.r) {
            return false;
        }
        if (Double.doubleToLongBits(theta) != Double.doubleToLongBits(other.theta)) {
            return false;
        }
        return true;
    }

    /**
     * Transform the polar point to the {@link Point} in rectangular coordinates. The rectangular coordinates has the
     * same origin as the polar coordinates.
     *
     * @return the point in rectangular coordinates
     */
    public Point toPoint() {
        var x = (int) (r * Math.cos(theta));
        var y = (int) (-r * Math.sin(theta));
        return new Point(x, y);
    }

    /**
     * Transform the polar point to the {@link Point} in the absolute coordinate system. It is assumed that the origin
     * of the polar coordinate system is the central point of the rectangle.
     *
     * @param rect
     *            the paint area of the figure
     * @return the point in absolute coordinate system.
     */
    public Point toAbsolutePoint(Rectangle rect) {
        var p = toPoint();
        return p.translate(rect.width / 2, rect.height / 2).translate(rect.x, rect.y);
    }

    /**
     * Transform the polar point to the {@link Point} in the relative coordinate system, whose origin is (rect.x,
     * rect.y). It is assumed that the origin of the polar coordinate system is the central point of the rectangle.
     *
     * @param rect
     *            the paint area of the figure
     * @return the point in relative coordinate system.
     */
    public Point toRelativePoint(Rectangle rect) {
        var p = toPoint();
        return p.translate(rect.width / 2, rect.height / 2);
    }

    /**
     * convert a point to polar point
     *
     * @param pole
     *            the pole of the polar coordinate system.
     * @param point
     *            the point to be converted
     * @return the corresponding polar point.
     */
    public static PolarPoint point2PolarPoint(Point pole, Point point) {
        var x = point.x - pole.x;
        var y = point.y - pole.y;

        var r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        var theta = Math.acos(x / r);
        if (y > 0) {
            theta = 2 * Math.PI - theta;
        }
        return new PolarPoint((int) r, theta);
    }

    /**
     * rotate the x axis of the polar coordinate system to the axisDirection
     *
     * @param axisDirection
     *            the direction of the new axis
     * @param inRadians
     *            true if the axisDirection is in radians, false if in degrees.
     */
    public void rotateAxis(double axisDirection, boolean inRadians) {
        if (!inRadians) {
            axisDirection = axisDirection * Math.PI / 180.0;
        }
        theta -= axisDirection;
        if (theta < 0) {
            theta += 2 * Math.PI;
        }
    }

    @Override
    public String toString() {
        return "(" + r + ", " + theta * 180.0 / Math.PI + ")";
    }
}
