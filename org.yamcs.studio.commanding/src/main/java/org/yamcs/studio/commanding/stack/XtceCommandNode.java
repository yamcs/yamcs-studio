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

import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.studio.core.ui.XtceTreeNode;

public class XtceCommandNode implements XtceTreeNode<CommandInfo> {

    private XtceTreeNode<CommandInfo> parent;
    private String name;
    private CommandInfo commandInfo;

    public XtceCommandNode(XtceTreeNode<CommandInfo> parent, String name, CommandInfo commandInfo) {
        this.parent = parent;
        this.name = name;
        this.commandInfo = commandInfo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public XtceTreeNode<CommandInfo> getParent() {
        return parent;
    }

    public CommandInfo getCommandInfo() {
        return commandInfo;
    }
}
