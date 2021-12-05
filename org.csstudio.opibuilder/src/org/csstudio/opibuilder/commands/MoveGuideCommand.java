/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.commands;

import java.util.Iterator;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.GuideModel;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

/**
 * A Command to move a Guide.
 */
public final class MoveGuideCommand extends Command {
    /**
     * The distance.
     */
    private int pDelta;
    /**
     * The guide, which position has changed.
     */
    private GuideModel guide;

    /**
     * Constructor.
     * 
     * @param guide
     *            the guide, which position has changed
     * @param pDelta
     *            the distance
     */
    public MoveGuideCommand(final GuideModel guide, final int pDelta) {
        this.pDelta = pDelta;
        this.guide = guide;
    }

    @Override
    public void execute() {
        guide.setPosition(guide.getPosition() + pDelta);
        Iterator<AbstractWidgetModel> iter = guide.getAttachedModels().iterator();
        while (iter.hasNext()) {
            AbstractWidgetModel model = iter.next();
            Point location = model.getLocation();
            if (guide.isHorizontal()) {
                location.y += pDelta;
            } else {
                location.x += pDelta;
            }
            model.setLocation(location.x, location.y);
        }
    }

    @Override
    public void undo() {
        guide.setPosition(guide.getPosition() - pDelta);
        Iterator<AbstractWidgetModel> iter = guide.getAttachedModels().iterator();
        while (iter.hasNext()) {
            AbstractWidgetModel model = iter.next();
            Point location = model.getLocation();
            if (guide.isHorizontal()) {
                location.y -= pDelta;
            } else {
                location.x -= pDelta;
            }
            model.setLocation(location.x, location.y);
        }
    }

}
