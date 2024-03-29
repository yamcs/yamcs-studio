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

public class RaiseIssueAction extends Action implements IWorkbenchAction {

    public static final String ID = "org.yamcs.studio.core.ui.actions.raiseIssue";

    public RaiseIssueAction() {
        super("Raise an Issue");
        setId(ID);
    }

    @Override
    public String getToolTipText() {
        return "Raise an Issue on GitHub";
    }

    @Override
    public void run() {
        Program.launch("https://github.com/yamcs/yamcs-studio/issues/");
    }

    @Override
    public void dispose() {
    }
}
