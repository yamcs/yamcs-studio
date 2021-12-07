/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.commands;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

/**
 * A command, which applies position and location changes to widget models.
 */
public final class SetBoundsCommand extends Command {
    /**
     * Stores the new size and location of the widget.
     */
    private final Rectangle newBounds;

    /**
     * Stores the old size and location.
     */
    private Rectangle oldBounds;

    /**
     * The element, whose constraints are to be changed.
     */
    private final AbstractWidgetModel widgetModel;

    /**
     * Create a command that can resize and/or move a widget model.
     *
     * @param widgetModel
     *            the widget model to manipulate
     * @param newBounds
     *            the new size and location
     */
    public SetBoundsCommand(AbstractWidgetModel widgetModel, Rectangle newBounds) {
        assert widgetModel != null;
        assert newBounds != null;
        this.widgetModel = widgetModel;
        this.newBounds = newBounds.getCopy();
        setLabel("Changing widget bounds");
    }

    @Override
    public void execute() {
        // remember old bounds
        oldBounds = new Rectangle(widgetModel.getLocation(), widgetModel.getSize());

        doApplyBounds(newBounds);
    }

    @Override
    public void undo() {
        doApplyBounds(oldBounds);
    }

    /**
     * Applies the specified bounds to the widget model.
     * 
     * @param bounds
     *            the bounds
     */
    private void doApplyBounds(Rectangle bounds) {
        // change element size
        widgetModel.setSize(bounds.width, bounds.height);

        // change location
        widgetModel.setLocation(bounds.x, bounds.y);
    }
}
