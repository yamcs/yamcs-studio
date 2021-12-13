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

import org.csstudio.opibuilder.editparts.AbstractLayoutEditpart;
import org.eclipse.jface.action.IAction;

/**
 * An action to layout widgets in a container.
 */
public class LayoutWidgetsAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {

        var layoutWidget = getLayoutWidget();

        LayoutWidgetsImp.run(layoutWidget, getCommandStack());
    }

    protected AbstractLayoutEditpart getLayoutWidget() {
        return (AbstractLayoutEditpart) selection.getFirstElement();
    }
}
