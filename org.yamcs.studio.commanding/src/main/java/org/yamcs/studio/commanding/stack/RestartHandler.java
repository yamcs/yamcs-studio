/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Resets the execution state, but keeps the stack intact
 */
public class RestartHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        CommandStack.getInstance().resetExecutionState();

        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);
        commandStackView.refreshState();
        commandStackView.selectFirst();
        return null;
    }
}
