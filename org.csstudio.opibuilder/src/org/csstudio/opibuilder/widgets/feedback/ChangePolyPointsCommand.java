/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import org.csstudio.opibuilder.widgets.model.AbstractPolyModel;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.commands.Command;

/**
 * A command, that changes the point list of a poly widget.
 */
public final class ChangePolyPointsCommand extends Command {
    /**
     * The model, whose points should be changed.
     */
    private AbstractPolyModel _polyModel;

    /**
     * The old point list.
     */
    private PointList _oldPoints;

    /**
     * The new point list.
     */
    private PointList _newPoints;

    /**
     * Constructor.
     * 
     * @param polyModel
     *            the polyline element, whose points should be changed
     * @param newPoints
     *            the new point list
     */
    public ChangePolyPointsCommand(AbstractPolyModel polyModel, PointList newPoints) {
        assert polyModel != null;
        assert newPoints != null;
        _polyModel = polyModel;
        _newPoints = newPoints;
    }

    @Override
    public void execute() {
        _oldPoints = _polyModel.getPoints();
        _polyModel.setPoints(_newPoints, true);
    }

    @Override
    public void undo() {
        _polyModel.setPoints(_oldPoints, true);
    }

}
