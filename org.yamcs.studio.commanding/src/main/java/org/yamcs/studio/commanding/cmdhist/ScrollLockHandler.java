/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

public class ScrollLockHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var part = HandlerUtil.getActivePartChecked(event);
        var view = (CommandHistoryView) part;

        var service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        var command = service.getCommand("org.yamcs.studio.commanding.cmdhist.scrollLockCommand");
        var oldState = HandlerUtil.toggleCommandState(command);
        view.enableScrollLock(!oldState);
        return null;
    }
}
