/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.util;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

public class GeometryUtil {

    /**
     * Get the range of children widgets.
     *
     * @param container
     *            editpart of the container widget.
     * @return the range (minX, minY, maxX-minX, maxY-minY) relative to the container.
     */
    public static Rectangle getChildrenRange(AbstractContainerEditpart container) {

        var pointList = new PointList(container.getChildren().size());
        for (var child : container.getChildren()) {
            var childModel = ((AbstractBaseEditPart) child).getWidgetModel();
            pointList.addPoint(childModel.getLocation());
            pointList.addPoint(childModel.getX() + childModel.getWidth(), childModel.getY() + childModel.getHeight());
        }
        return pointList.getBounds();
    }
}
