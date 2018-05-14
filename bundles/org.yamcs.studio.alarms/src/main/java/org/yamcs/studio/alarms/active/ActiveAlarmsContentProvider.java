package org.yamcs.studio.alarms.active;

import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.studio.core.ui.XtceTreeContentProvider;
import org.yamcs.studio.core.ui.XtceTreeNode;

public class ActiveAlarmsContentProvider extends XtceTreeContentProvider<AlarmData> {

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    protected XtceTreeNode<AlarmData> createXtceTreeNode(XtceTreeNode<AlarmData> parent, String name, AlarmData data) {
        return new XtceAlarmNode(parent, name, data);
    }
}
