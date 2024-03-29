/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.core.YamcsPlugin;

public class RestartInstanceHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(RestartInstanceHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);

        var client = YamcsPlugin.getYamcsClient();
        var instance = YamcsPlugin.getInstance();
        client.restartInstance(instance).whenComplete((ret, ex) -> {
            log.log(Level.SEVERE, "Failed to restart instance '" + instance + "'", ex);
            Display.getDefault().asyncExec(() -> {
                MessageDialog.openError(shell, "Failed to restart instance '" + instance + "'", ex.getMessage());
            });
        });

        return null;
    }
}
