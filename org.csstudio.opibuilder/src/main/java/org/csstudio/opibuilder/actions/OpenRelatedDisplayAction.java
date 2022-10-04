/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.widgetActions.OpenDisplayAction;
import org.eclipse.jface.action.Action;

/**
 * Context menu wrapper for {@link OpenDisplayAction}.
 *
 * <p>
 * On a simple click, the underlying OpenDisplayAction is executed.
 *
 * <p>
 * When opening the context menu on a widget with actions, instances of this class will be listed, all referring to the
 * same OpenDisplayAction, but allowing user to open the display in different ways.
 */
public class OpenRelatedDisplayAction extends Action {
    public enum OpenDisplayTarget {
        DEFAULT("Open"),
        NEW_TAB("Open in Workbench Tab"),
        NEW_WINDOW("Open in New Workbench"),
        NEW_SHELL("Open in Standalone Window");

        private String description;

        OpenDisplayTarget(String desc) {
            description = desc;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    final private OpenDisplayAction openDisplayAction;

    final private OpenDisplayTarget target;

    public OpenRelatedDisplayAction(OpenDisplayAction openDisplayAction, OpenDisplayTarget target) {
        this.openDisplayAction = openDisplayAction;
        this.target = target;
        setText(target.toString());
    }

    @Override
    public void run() {
        switch (target) {
        case NEW_TAB:
            openDisplayAction.runWithModifiers(true, false);
            break;
        case NEW_WINDOW:
            openDisplayAction.runWithModifiers(false, true);
            break;
        case NEW_SHELL:
            openDisplayAction.runWithModifiers(true, true);
            break;
        default:
            openDisplayAction.run();
            break;
        }
    }
}
