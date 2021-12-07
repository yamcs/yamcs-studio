/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.commands;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.GuideModel;
import org.csstudio.opibuilder.model.RulerModel;
import org.eclipse.gef.commands.Command;

/**
 * A Command to delete a guide.
 */
public final class DeleteGuideCommand extends Command {

    private RulerModel parent;
    private GuideModel guide;
    private Map<AbstractWidgetModel, Integer> oldParts;

    public DeleteGuideCommand(GuideModel guide, RulerModel parent) {
        this.guide = guide;
        this.parent = parent;
    }

    @Override
    public void execute() {
        oldParts = new HashMap<AbstractWidgetModel, Integer>(guide.getMap());
        var iter = oldParts.keySet().iterator();
        while (iter.hasNext()) {
            guide.detachPart(iter.next());
        }
        parent.removeGuide(guide);
    }

    @Override
    public void undo() {
        parent.addGuide(guide);
        var iter = oldParts.keySet().iterator();
        while (iter.hasNext()) {
            var model = iter.next();
            guide.attachPart(model, ((Integer) oldParts.get(model)).intValue());
        }
    }
}
