package org.yamcs.studio.editor.base.views;

import org.yamcs.studio.core.ui.XtceTreeNode;
import org.yamcs.xtce.Parameter;

public class XtceParameterNode implements XtceTreeNode<Parameter> {

    private XtceTreeNode<Parameter> parent;
    private String name;
    private Parameter parameter;

    public XtceParameterNode(XtceTreeNode<Parameter> parent, String name, Parameter parameter) {
        this.parent = parent;
        this.name = name;
        this.parameter = parameter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public XtceTreeNode<Parameter> getParent() {
        return parent;
    }

    public Parameter getParameter() {
        return parameter;
    }
}
