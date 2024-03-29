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

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

/**
 * We have multiple implementations of the Anchor, and the {@link FixedPointsConnectionRouter} needs to know orientation
 * in which the connector connects to the widget in order to correctly calculate the route. This becomes especially
 * important once the widgets change positions (are animated) through some action.
 */
abstract public class AbstractOpiBuilderAnchor extends AbstractConnectionAnchor {

    /**
     * This enum tells in which direction a connector is connected to the widget
     */
    public enum ConnectorOrientation {
        HORIZONTAL, VERTICAL
    }

    public AbstractOpiBuilderAnchor(IFigure owner) {
        super(owner);
    }

    /**
     * <p>
     * In case of linking container zoom edge cases appear when linked OPI contains elements staring on point(0,0). When
     * zooming some anchor points get out of linked container box. This causes broken connection, as anchor is no longer
     * found.
     * <p>
     * This is fixed by moving the anchor back into the bounds of linking container.
     *
     * @param point
     *            - reference to anchor point
     * @param owner
     *            - reference to the figure
     *
     */

    public static void fixZoomEdgeRounding(Point point, IFigure owner) {
        var checkPoint = point.getCopy();
        owner.getParent().translateToRelative(checkPoint);
        if (checkPoint.x < 0) {
            point = point.translate(Math.abs(checkPoint.x), 0);
        }
        if (checkPoint.y < 0) {
            point = point.translate(0, Math.abs(checkPoint.y));
        }
    }

    /**
     * @return The direction in which the connection is oriented at this anchor, if the Manhattan router is being
     *         selected
     */
    abstract public ConnectorOrientation getOrientation();
}
