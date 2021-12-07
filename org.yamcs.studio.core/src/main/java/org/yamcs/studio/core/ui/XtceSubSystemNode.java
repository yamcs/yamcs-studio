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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class XtceSubSystemNode implements XtceTreeNode {

    private XtceTreeNode parent;
    private String name;

    private Map<String, XtceTreeNode> children = new LinkedHashMap<>();

    public XtceSubSystemNode(XtceTreeNode parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public XtceTreeNode getParent() {
        return parent;
    }

    public Collection<XtceTreeNode> getChildren() {
        return children.values();
    }

    public XtceTreeNode getChild(String name) {
        return children.get(name);
    }

    public void addChild(XtceTreeNode child) {
        children.put(child.getName(), child);
    }
}
