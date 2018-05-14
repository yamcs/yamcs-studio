package org.yamcs.studio.alarms.active;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.model.AlarmCatalogue;
import org.yamcs.studio.core.model.AlarmListener;
import org.yamcs.studio.core.ui.XtceSubSystemNode;

public class ActiveAlarmsView extends ViewPart implements AlarmListener {

    private TreeViewer viewer;
    private ActiveAlarmsContentProvider contentProvider;

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        contentProvider = new ActiveAlarmsContentProvider();
        viewer.setContentProvider(contentProvider);
        viewer.setInput(contentProvider);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(true);

        TreeViewerColumn nameColumn = new TreeViewerColumn(viewer, SWT.NONE);
        nameColumn.getColumn().setWidth(300);
        nameColumn.getColumn().setText("Alarm");
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    return ((XtceAlarmNode) element).getName();
                } else {
                    return ((XtceSubSystemNode) element).getName();
                }
            }
        });

        TreeViewerColumn triggeredColumn = new TreeViewerColumn(viewer, SWT.NONE);
        triggeredColumn.getColumn().setWidth(300);
        triggeredColumn.getColumn().setText("Date");
        triggeredColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    return String.valueOf(alarmData.getTriggerValue().getGenerationTimeUTC());
                } else {
                    return null;
                }
            }
        });

        TreeViewerColumn currentSeverityColumn = new TreeViewerColumn(viewer, SWT.NONE);
        currentSeverityColumn.getColumn().setWidth(300);
        currentSeverityColumn.getColumn().setText("Current Severity");
        currentSeverityColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    return String.valueOf(alarmData.getCurrentValue().getMonitoringResult());
                } else {
                    return null;
                }
            }
        });

        TreeViewerColumn triggeredSeverityColumn = new TreeViewerColumn(viewer, SWT.NONE);
        triggeredSeverityColumn.getColumn().setWidth(300);
        triggeredSeverityColumn.getColumn().setText("Severity");
        triggeredSeverityColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    return String.valueOf(alarmData.getTriggerValue().getMonitoringResult());
                } else {
                    return null;
                }
            }
        });

        TreeViewerColumn typeColumn = new TreeViewerColumn(viewer, SWT.NONE);
        typeColumn.getColumn().setWidth(300);
        typeColumn.getColumn().setText("Type");
        typeColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    // AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    return "Out of Limits";
                } else {
                    return null;
                }
            }
        });

        TreeViewerColumn valueColumn = new TreeViewerColumn(viewer, SWT.NONE);
        valueColumn.getColumn().setWidth(300);
        valueColumn.getColumn().setText("Value");
        valueColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    return alarmData.getTriggerValue().getEngValue().toString();
                } else {
                    return null;
                }
            }
        });

        TreeViewerColumn violationsColumn = new TreeViewerColumn(viewer, SWT.NONE);
        violationsColumn.getColumn().setWidth(300);
        violationsColumn.getColumn().setText("Violations");
        violationsColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    return String.valueOf(alarmData.getViolations());
                } else {
                    return null;
                }
            }
        });

        GridLayoutFactory.fillDefaults().generateLayout(parent);

        viewer.getTree().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;
                if (item.getItemCount() > 0) {
                    item.setExpanded(!item.getExpanded());
                    viewer.refresh();
                }
            }
        });

        viewer.addDoubleClickListener(event -> {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            Object selectedNode = sel.getFirstElement();
            viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
        });

        getSite().setSelectionProvider(viewer);

        // Set initial state
        viewer.refresh();

        AlarmCatalogue.getInstance().addAlarmListener(this);
    }

    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
    }

    @Override
    public void dispose() {
        AlarmCatalogue.getInstance().removeAlarmListener(this);
        super.dispose();
    }

    @Override
    public void processAlarmData(AlarmData alarmData) {
        Display.getDefault().asyncExec(() -> {
            ParameterValue triggerValue = alarmData.getTriggerValue();
            String qname = triggerValue.getId().getName();
            if (!qname.startsWith("/")) {
                throw new IllegalArgumentException("Unexpected id " + qname);
            }
            contentProvider.addElement(qname, alarmData);
            viewer.refresh();
        });
    }
}
