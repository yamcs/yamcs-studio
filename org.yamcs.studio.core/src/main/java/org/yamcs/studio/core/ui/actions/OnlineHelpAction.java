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
import org.eclipse.swt.program.Program;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class OnlineHelpAction extends Action implements IWorkbenchAction {

    public static final String ID = "org.yamcs.studio.core.ui.actions.onlineHelp";

    public OnlineHelpAction() {
        super("Yamcs Studio Help");
        setId(ID);
    }

    @Override
    public String getToolTipText() {
        return "Open Online Documentation";
    }

    @Override
    public void run() {
        Program.launch("https://yamcs.org/docs/");
    }

    @Override
    public void dispose() {
    }
}
