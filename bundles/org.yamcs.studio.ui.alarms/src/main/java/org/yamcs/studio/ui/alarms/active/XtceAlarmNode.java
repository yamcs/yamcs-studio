package org.yamcs.studio.ui.alarms.active;

import org.yamcs.protobuf.Alarms.AlarmData;

public class XtceAlarmNode implements XtceTreeNode {

    private XtceTreeNode parent;
    private String name;
    private AlarmData alarmData;

    public XtceAlarmNode(XtceTreeNode parent, String name, AlarmData alarmData) {
        this.parent = parent;
        this.name = name;
        this.alarmData = alarmData;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public XtceTreeNode getParent() {
        return parent;
    }

    public AlarmData getAlarmData() {
        return alarmData;
    }
}
