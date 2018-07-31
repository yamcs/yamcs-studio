package org.yamcs.studio.editor.base.views;

import org.eclipse.jface.viewers.Viewer;
import org.yamcs.studio.core.ui.XtceTreeContentProvider;
import org.yamcs.studio.core.ui.XtceTreeNode;
import org.yamcs.xtce.Parameter;

public class ParametersContentProvider extends XtceTreeContentProvider<Parameter> {

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    protected XtceTreeNode<Parameter> createXtceTreeNode(XtceTreeNode<Parameter> parent, String name,
            Parameter parameter) {
        return new XtceParameterNode(parent, name, parameter);
    }
}
