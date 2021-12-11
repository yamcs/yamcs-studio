/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.eclipse.jface.action.IAction;

/**
 * The action will select parent container of current selected widget.
 */
public class SelectParentAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {

        var containerEditpart = getParentContainerEditpart();
        containerEditpart.getViewer().select(containerEditpart);

    }

    protected final AbstractContainerEditpart getParentContainerEditpart() {
        return (AbstractContainerEditpart) ((AbstractBaseEditPart) selection.getFirstElement()).getParent();
    }

}
