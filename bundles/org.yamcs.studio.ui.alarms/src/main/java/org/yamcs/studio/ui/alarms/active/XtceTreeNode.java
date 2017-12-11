package org.yamcs.studio.ui.alarms.active;

/**
 * Can be either an XTCE item or an XTCE subsystem
 */
public interface XtceTreeNode {

    public String getName();

    public XtceTreeNode getParent();

}
