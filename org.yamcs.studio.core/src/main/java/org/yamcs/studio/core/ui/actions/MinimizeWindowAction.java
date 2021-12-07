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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class MinimizeWindowAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public MinimizeWindowAction(IWorkbenchWindow window) {
        super("Minimize");
        this.window = window;
    }

    @Override
    public void run() {
        window.getShell().setMinimized(true);
    }

    @Override
    public void dispose() {
        window = null;
    }
}
