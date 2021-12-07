/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.commands;

import org.csstudio.opibuilder.model.ConnectionModel;
import org.eclipse.gef.commands.Command;

/**
 * A command to disconnect (remove) a connection from its endpoints.
 */
public class ConnectionDeleteCommand extends Command {

    /** Connection Model */
    private final ConnectionModel connection;

    /**
     * Create a command that will disconnect a connection from its endpoints.
     *
     * @param conn
     *            the connection model (non-null)
     * @throws IllegalArgumentException
     *             if conn is null
     */
    public ConnectionDeleteCommand(ConnectionModel conn) {
        if (conn == null) {
            throw new IllegalArgumentException();
        }
        setLabel("Delete Connection");
        this.connection = conn;
    }

    @Override
    public void execute() {
        connection.disconnect();
    }

    @Override
    public void undo() {
        connection.reconnect();
    }
}
