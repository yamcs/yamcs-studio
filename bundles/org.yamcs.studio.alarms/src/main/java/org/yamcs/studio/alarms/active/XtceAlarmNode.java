package org.yamcs.studio.alarms.active;

import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.studio.core.ui.XtceTreeNode;

public class XtceAlarmNode implements XtceTreeNode<AlarmData> {

    private XtceTreeNode<AlarmData> parent;
    private String name;
    private AlarmData alarmData;

    public XtceAlarmNode(XtceTreeNode<AlarmData> parent, String name, AlarmData alarmData) {
        this.parent = parent;
        this.name = name;
        this.alarmData = alarmData;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public XtceTreeNode<AlarmData> getParent() {
        return parent;
    }

    public AlarmData getAlarmData() {
        return alarmData;
    }
}
