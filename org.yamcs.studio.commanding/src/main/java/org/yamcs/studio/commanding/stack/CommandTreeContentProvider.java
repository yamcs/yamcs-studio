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

import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.studio.core.ui.XtceTreeContentProvider;
import org.yamcs.studio.core.ui.XtceTreeNode;

public class CommandTreeContentProvider extends XtceTreeContentProvider<CommandInfo> {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    protected XtceTreeNode<CommandInfo> createXtceTreeNode(XtceTreeNode<CommandInfo> parent, String name,
            CommandInfo data) {
        return new XtceCommandNode(parent, name, data);
    }
}
