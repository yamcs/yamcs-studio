/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * The anchor is on a fixed position of the figure. For example, left, right, top left, ...
 */
public class FixedPositionAnchor extends AbstractOpiBuilderAnchor {

    public enum AnchorPosition {
        TOP, LEFT, RIGHT, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
    }

    private AnchorPosition anchorPosition;

    @Override
    public ConnectorOrientation getOrientation() {
        switch (anchorPosition) {
        case LEFT:
        case RIGHT:
            return ConnectorOrientation.HORIZONTAL;
        case BOTTOM:
        case BOTTOM_LEFT:
        case BOTTOM_RIGHT:
        case TOP:
        case TOP_LEFT:
        case TOP_RIGHT:
            return ConnectorOrientation.VERTICAL;
        default:
            throw new IllegalStateException("Unknown constant of " + AnchorPosition.class.getCanonicalName() + ": "
                    + anchorPosition.toString());
        }
    }

    public FixedPositionAnchor(IFigure owner, AnchorPosition anchorPosition) {
        super(owner);
        this.anchorPosition = anchorPosition;
    }

    /**
     * Returns the bounds of this ChopboxAnchor's owner. Subclasses can override this method to adjust the box the
     * anchor can be placed on. For instance, the owner figure may have a drop shadow that should not be included in the
     * box.
     *
     * @return The bounds of this ChopboxAnchor's owner
     */
    protected Rectangle getBox() {
        return getOwner().getBounds();
    }

    @Override
    public Point getReferencePoint() {
        return getLocation(null);
    }

    @Override
    public Point getLocation(Point reference) {
        var box = getBox();
        int x = box.x, y = box.y;
        switch (anchorPosition) {
        case BOTTOM:
        case BOTTOM_LEFT:
        case BOTTOM_RIGHT:
            y = box.y + box.height;
            break;
        // case CENTER:
        case LEFT:
        case RIGHT:
            y = box.y + box.height / 2;
            break;
        case TOP:
        case TOP_LEFT:
        case TOP_RIGHT:
            y = box.y;
            break;
        default:
            break;
        }

        switch (anchorPosition) {
        case LEFT:
        case BOTTOM_LEFT:
        case TOP_LEFT:
            x = box.x;
            break;
        // case CENTER:
        case TOP:
        case BOTTOM:
            x = box.x + box.width / 2;
            break;
        case BOTTOM_RIGHT:
        case RIGHT:
        case TOP_RIGHT:
            x = box.x + box.width;
            break;
        default:
            break;
        }
        var p = new Point(x, y);
        getOwner().translateToAbsolute(p);
        fixZoomEdgeRounding(p, getOwner());
        return p;
    }

    /**
     * Returns <code>true</code> if the other anchor has the same owner and box.
     *
     * @param obj
     *            the other anchor
     * @return <code>true</code> if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FixedPositionAnchor) {
            var other = (FixedPositionAnchor) obj;
            return other.getOwner() == getOwner() && other.getBox().equals(getBox())
                    && other.anchorPosition == anchorPosition;
        }
        return false;
    }

    /**
     * The owning figure's hashcode is used since equality is approximately based on the owner.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        if (getOwner() != null) {
            return getOwner().hashCode() ^ (anchorPosition.ordinal() + 31);
        } else {
            return super.hashCode();
        }
    }
}
