package org.yamcs.studio.alarms.active;

/**
 * Can be either an XTCE item or an XTCE subsystem
 */
public interface XtceTreeNode {

    public String getName();

    public XtceTreeNode getParent();

}
