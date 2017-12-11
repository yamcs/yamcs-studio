package org.yamcs.studio.ui.alarms.active;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class ActiveAlarmsContentProvider implements ITreeContentProvider {

    private Map<String, XtceTreeNode> roots = new LinkedHashMap<>();

    @Override
    public Object[] getElements(Object inputElement) {
        return roots.values().toArray();
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof XtceSubSystemNode;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof XtceSubSystemNode) {
            XtceSubSystemNode subSystem = (XtceSubSystemNode) parentElement;
            return subSystem.getChildren().toArray();
        } else {
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        XtceTreeNode node = (XtceTreeNode) element;
        return node.getParent();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    /**
     * Fits the new or updated alarm data in the current model
     */
    void processActiveAlarm(AlarmData alarmData) {
        ParameterValue triggerValue = alarmData.getTriggerValue();
        String qname = triggerValue.getId().getName();
        if (!qname.startsWith("/")) {
            throw new IllegalArgumentException("Unexpected alarm id " + qname);
        }

        String[] parts = qname.split("\\/");

        boolean isDirectLeaf = parts.length == 1;
        if (isDirectLeaf) {
            String name = parts[1];
            roots.put(name, new XtceAlarmNode(null, name, alarmData));
        } else {
            XtceSubSystemNode root = findOrCreateRootSpaceSystem(parts[1]);
            XtceSubSystemNode parent = root;
            for (int i = 2; i < parts.length - 1; i++) {
                XtceTreeNode node = parent.getChild(parts[i]);
                if (node == null) {
                    node = new XtceSubSystemNode(parent, parts[i]);
                    parent.addChild(node);
                }
                parent = (XtceSubSystemNode) node;
            }
            String name = parts[parts.length - 1];
            parent.addChild(new XtceAlarmNode(parent, name, alarmData));
        }
    }

    private XtceSubSystemNode findOrCreateRootSpaceSystem(String name) {
        XtceTreeNode root = roots.get(name);
        if (root == null) {
            XtceSubSystemNode newRoot = new XtceSubSystemNode(null, name);
            roots.put(name, newRoot);
            return newRoot;
        } else if (root instanceof XtceSubSystemNode) {
            return (XtceSubSystemNode) root;
        } else {
            return null;
        }
    }
}
