/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.editparts;

import java.util.HashMap;

import org.csstudio.opibuilder.editparts.PolyGraphAnchor;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.AbstractPolyModel;
import org.csstudio.swt.widgets.util.PointsUtil;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
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

        // points
        IWidgetPropertyChangeHandler pointsHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(final Object oldValue,
                    final Object newValue,
                    final IFigure refreshableFigure) {
                Polyline polyline = (Polyline) refreshableFigure;

                PointList points = (PointList) newValue;
                if (points.size() != polyline.getPoints().size()) {
                    anchorMap = null;
                    // delete connections on deleted points
                    if (points.size() < polyline.getPoints().size()) {
                        for (ConnectionModel conn : getWidgetModel().getSourceConnections()) {
                            if (Integer.parseInt(conn.getSourceTerminal()) >= points.size()) {
                                conn.disconnect();
                            }
                        }
                        for (ConnectionModel conn : getWidgetModel().getTargetConnections()) {
                            if (Integer.parseInt(conn.getTargetTerminal()) >= points.size()) {
                                conn.disconnect();
                            }
                        }
                    }
                }
                // deselect the widget (this refreshes the polypoint drag
                // handles)
                int selectionState = getSelected();
                setSelected(EditPart.SELECTED_NONE);

                polyline.setPoints(points);
                doRefreshVisuals(polyline);

                // restore the selection state
                setSelected(selectionState);

                return false;
            }
        };
        setPropertyChangeHandler(AbstractPolyModel.PROP_POINTS, pointsHandler);

        IWidgetPropertyChangeHandler rotationHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue,
                    IFigure figure) {
                getWidgetModel().setPoints(
                        PointsUtil.rotatePoints(getWidgetModel().getOriginalPoints().getCopy(),
                                (Double) newValue),
                        false);
                return false;
            }
        };

        setPropertyChangeHandler(AbstractPolyModel.PROP_ROTATION, rotationHandler);

    }

    @Override
    public Polyline getFigure() {
        return (Polyline) super.getFigure();
    }

    @Override
    protected void fillAnchorMap() {
        anchorMap = new HashMap<String, ConnectionAnchor>(getFigure().getPoints().size());
        for (int i = 0; i < getFigure().getPoints().size(); i++) {
            anchorMap.put(Integer.toString(i), new PolyGraphAnchor(getFigure(), i));
        }
    }
}
