/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.connections;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.connect.ConnectionPreferences;
import org.yamcs.studio.connect.ConnectionsDialog;
import org.yamcs.studio.connect.YamcsConfiguration;
import org.yamcs.studio.core.YamcsConnector;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Pops up the connection manager dialog.
 */
public class ConnectHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        var dialog = new ConnectionsDialog(window.getShell());
        if (dialog.open() == Dialog.OK) {
            var conf = dialog.getSelectedConfiguration();
            doConnect(HandlerUtil.getActiveShell(event), conf);
        }

        return null;
    }

    private void doConnect(Shell shell, YamcsConfiguration conf) {
        YamcsPlugin.disconnect(false /* lost */);

        ConnectionPreferences.setLastUsedConnection(conf.getId());
        try {
            var connector = new YamcsConnector(shell, conf);
            new ProgressMonitorDialog(shell).run(true, true, connector);
        } catch (InvocationTargetException e) {
            log.log(Level.SEVERE, "Failed to connect", e);
            MessageDialog.openError(shell, "Failed to connect", e.getMessage());
        } catch (InterruptedException e) {
            log.info("Connection attempt cancelled");
        }
    }
}

/*
 *     @Override
    public void onYamcsConnectionFailed(Throwable t) {
        Display.getDefault().asyncExec(() -> {
            if (t.getMessage() != null && t.getMessage().contains("401")) {
                // Show Login Pane
                RCPUtils.runCommand("org.yamcs.studio.ui.login");
            }
        });
    }
 */
