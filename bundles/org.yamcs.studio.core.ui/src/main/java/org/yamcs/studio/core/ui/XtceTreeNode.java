package org.yamcs.studio.core.ui;

/**
 * Can be either an XTCE item or an XTCE subsystem
 */
public interface XtceTreeNode<T> {

    public String getName();

    public XtceTreeNode<T> getParent();
}
