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
