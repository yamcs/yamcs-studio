/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class BringAllToFrontAction extends Action implements IWorkbenchAction {

    private IWorkbench workbench;

    public BringAllToFrontAction(IWorkbench workbench) {
        super("Bring All to Front");
        this.workbench = workbench;
    }

    @Override
    public void run() {
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            window.getShell().setActive();
        }
    }

    @Override
    public void dispose() {
        workbench = null;
    }
}
