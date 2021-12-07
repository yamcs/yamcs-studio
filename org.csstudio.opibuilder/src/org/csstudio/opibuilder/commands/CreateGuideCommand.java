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

import org.csstudio.opibuilder.model.GuideModel;
import org.csstudio.opibuilder.model.RulerModel;
import org.eclipse.gef.commands.Command;

/**
 * A Command to create a Guide.
 */
public final class CreateGuideCommand extends Command {

    private int _position;

    private RulerModel _parent;

    private GuideModel _guide;

    public CreateGuideCommand(RulerModel parent, int position) {
        super();
        _parent = parent;
        _position = position;
    }

    @Override
    public void execute() {
        _guide = new GuideModel(_position);
        _parent.addGuide(_guide);
    }

    @Override
    public void undo() {
        _parent.removeGuide(_guide);
    }

    @Override
    public void redo() {
        _parent.addGuide(_guide);
    }

}
