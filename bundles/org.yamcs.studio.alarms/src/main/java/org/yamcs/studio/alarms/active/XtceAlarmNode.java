package org.yamcs.studio.alarms.active;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        // Compare equality based on Alarm ID, the main use of this is to
        // automatically restore selection when the alarm table is updated.
        if (obj == null || !(obj instanceof XtceAlarmNode)) {
            return false;
        }
        XtceAlarmNode other = (XtceAlarmNode) obj;
        return Objects.equals(alarmData.getTriggerValue().getGenerationTime(),
                other.alarmData.getTriggerValue().getGenerationTime())
                && Objects.equals(alarmData.getParameter().getQualifiedName(),
                        other.alarmData.getParameter().getQualifiedName())
                && Objects.equals(alarmData.getSeqNum(), other.alarmData.getSeqNum());
    }
}
