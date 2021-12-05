/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.commands;

import org.csstudio.opibuilder.actions.ChangeOrientationAction.OrientationType;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.gef.commands.Command;

/**
 * Flip a widget horizontally.
 */
public class ChangeOrientationCommand extends Command {

    private AbstractWidgetModel widgetModel;

    private OrientationType orientationType;

    public ChangeOrientationCommand(AbstractWidgetModel widgetModel,
            OrientationType orientationType) {
        this.widgetModel = widgetModel;
        this.orientationType = orientationType;
    }

    @Override
    public void execute() {
        switch (orientationType) {
        case FLIP_HORIZONTAL:
            widgetModel.flipHorizontally();
            break;
        case FLIP_VERTICAL:
            widgetModel.flipVertically();
            break;
        case ROTATE_CLOCKWISE:
            widgetModel.rotate90(true);
            break;
        case ROTATE_COUNTERCLOCKWISE:
            widgetModel.rotate90(false);
            break;
        default:
            break;
        }

    }

    @Override
    public void undo() {
        switch (orientationType) {
        case FLIP_HORIZONTAL:
            widgetModel.flipHorizontally();
            break;
        case FLIP_VERTICAL:
            widgetModel.flipVertically();
            break;
        case ROTATE_CLOCKWISE:
            widgetModel.rotate90(false);
            break;
        case ROTATE_COUNTERCLOCKWISE:
            widgetModel.rotate90(true);
            break;
        default:
            break;
        }
    }

}
