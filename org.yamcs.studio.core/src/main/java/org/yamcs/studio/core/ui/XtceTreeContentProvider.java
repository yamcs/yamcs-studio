package org.yamcs.studio.core.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;

public abstract class XtceTreeContentProvider<T> implements ITreeContentProvider {

    private Map<String, XtceTreeNode<T>> roots = new LinkedHashMap<>();

    @Override
    public Object[] getElements(Object inputElement) {
        return roots.values().toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof XtceSubSystemNode) {
            var subSystem = (XtceSubSystemNode) parentElement;
            return subSystem.getChildren().toArray();
        } else {
            return new Object[0];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getParent(Object element) {
        var node = (XtceTreeNode<T>) element;
        return node.getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof XtceSubSystemNode;
    }

    protected abstract XtceTreeNode<T> createXtceTreeNode(XtceTreeNode<T> parent, String name, T data);

    /**
     * Fits the XTCE element in the current model
     */
    @SuppressWarnings("unchecked")
    public void addElement(String qualifiedName, T data) {
        var parts = qualifiedName.split("\\/");

        var isDirectLeaf = parts.length == 1;
        if (isDirectLeaf) {
            var name = parts[1];
            roots.put(name, createXtceTreeNode(null, name, data));
        } else {
            var root = findOrCreateRootSpaceSystem(parts[1]);
            var parent = root;
            for (var i = 2; i < parts.length - 1; i++) {
                XtceTreeNode<T> node = parent.getChild(parts[i]);
                if (node == null) {
                    node = new XtceSubSystemNode(parent, parts[i]);
                    parent.addChild(node);
                }
                parent = (XtceSubSystemNode) node;
            }
            var name = parts[parts.length - 1];
            parent.addChild(createXtceTreeNode(parent, name, data));
        }
    }

    @SuppressWarnings("unchecked")
    private XtceSubSystemNode findOrCreateRootSpaceSystem(String name) {
        var root = roots.get(name);
        if (root == null) {
            var newRoot = new XtceSubSystemNode(null, name);
            roots.put(name, newRoot);
            return newRoot;
        } else if (root instanceof XtceSubSystemNode) {
            return (XtceSubSystemNode) root;
        } else {
            return null;
        }
    }
}
