/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.widgets.model.AbstractPolyModel.PROP_POINTS;
import static org.csstudio.opibuilder.widgets.model.AbstractPolyModel.PROP_ROTATION;

import java.util.HashMap;

import org.csstudio.opibuilder.editparts.PolyGraphAnchor;
import org.csstudio.opibuilder.widgets.model.AbstractPolyModel;
import org.csstudio.swt.widgets.util.PointsUtil;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.EditPart;

/**
 * Abstract EditPart controller for the Polyline/polygon widget.
 */
public abstract class AbstractPolyEditPart extends AbstractShapeEditPart {

    @Override
    public AbstractPolyModel getWidgetModel() {
        return (AbstractPolyModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_POINTS, (oldValue, newValue, refreshableFigure) -> {
            var polyline = (Polyline) refreshableFigure;

            var points = (PointList) newValue;
            if (points.size() != polyline.getPoints().size()) {
                anchorMap = null;
                // delete connections on deleted points
                if (points.size() < polyline.getPoints().size()) {
                    for (var conn1 : getWidgetModel().getSourceConnections()) {
                        if (Integer.parseInt(conn1.getSourceTerminal()) >= points.size()) {
                            conn1.disconnect();
                        }
                    }
                    for (var conn2 : getWidgetModel().getTargetConnections()) {
                        if (Integer.parseInt(conn2.getTargetTerminal()) >= points.size()) {
                            conn2.disconnect();
                        }
                    }
                }
            }
            // deselect the widget (this refreshes the polypoint drag handles)
            var selectionState = getSelected();
            setSelected(EditPart.SELECTED_NONE);

            polyline.setPoints(points);
            doRefreshVisuals(polyline);

            // restore the selection state
            setSelected(selectionState);

            return false;
        });

        setPropertyChangeHandler(PROP_ROTATION, (oldValue, newValue, figure) -> {
            getWidgetModel().setPoints(
                    PointsUtil.rotatePoints(getWidgetModel().getOriginalPoints().getCopy(), (Double) newValue),
                    false);
            return false;
        });
    }

    @Override
    public Polyline getFigure() {
        return (Polyline) super.getFigure();
    }

    @Override
    protected void fillAnchorMap() {
        anchorMap = new HashMap<>(getFigure().getPoints().size());
        for (var i = 0; i < getFigure().getPoints().size(); i++) {
            anchorMap.put(Integer.toString(i), new PolyGraphAnchor(getFigure(), i));
        }
    }
}
