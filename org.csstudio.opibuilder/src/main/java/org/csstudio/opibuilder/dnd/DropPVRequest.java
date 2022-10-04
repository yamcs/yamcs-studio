/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.dnd;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;

/**
 * The request to drop a pv on a target.
 */
public class DropPVRequest extends Request implements DropRequest {

    public final static String REQ_DROP_PV = "drop pv";

    private String[] pvNames;

    private Point location;

    private AbstractBaseEditPart targetWidget;

    public DropPVRequest() {
        setType(REQ_DROP_PV);
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public void setPvNames(String[] pvNames) {
        this.pvNames = pvNames;
    }

    public String[] getPvNames() {
        return pvNames;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    /**
     * @param targetWidget
     *            the targetWidget to set
     */
    public void setTargetWidget(AbstractBaseEditPart targetWidget) {
        this.targetWidget = targetWidget;
    }

    /**
     * @return the targetWidget
     */
    public AbstractBaseEditPart getTargetWidget() {
        return targetWidget;
    }
}
