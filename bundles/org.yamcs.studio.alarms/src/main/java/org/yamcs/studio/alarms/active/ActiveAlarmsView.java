package org.yamcs.studio.alarms.active;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.protobuf.Alarms.ParameterAlarmData;
import org.yamcs.protobuf.Mdb.UnitInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Pvalue.RangeCondition;
import org.yamcs.studio.alarms.AlarmsPlugin;
import org.yamcs.studio.core.model.AlarmCatalogue;
import org.yamcs.studio.core.model.AlarmListener;
import org.yamcs.studio.core.ui.XtceSubSystemNode;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.utils.StringConverter;

public class ActiveAlarmsView extends ViewPart implements AlarmListener {

    private TreeViewer viewer;
    private ActiveAlarmsContentProvider contentProvider;

    private Image infoIcon;
    private Image watchIcon;
    private Image warningIcon;
    private Image distressIcon;
    private Image criticalIcon;
    private Image severeIcon;

    @Override
    public void createPartControl(Composite parent) {
        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        AlarmsPlugin plugin = AlarmsPlugin.getDefault();
        infoIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/level0s.png"));
        watchIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/level1s.png"));
        warningIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/level2s.png"));
        distressIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/level3s.png"));
        criticalIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/level4s.png"));
        severeIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/level5s.png"));

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
        triggeredColumn.getColumn().setWidth(160);
        triggeredColumn.getColumn().setText("Time");
        triggeredColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    ParameterAlarmData parameterDetail = alarmData.getParameterDetail();
                    return YamcsUIPlugin.getDefault().formatInstant(
                            parameterDetail.getTriggerValue().getGenerationTime());
                } else {
                    return null;
                }
            }
        });

        TreeViewerColumn valueColumn = new TreeViewerColumn(viewer, SWT.NONE);
        valueColumn.getColumn().setWidth(100);
        valueColumn.getColumn().setText("Trigger Value");
        valueColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    ParameterAlarmData parameterDetail = alarmData.getParameterDetail();
                    ParameterValue pval = parameterDetail.getTriggerValue();

                    String stringValue = StringConverter.toString(pval.getEngValue(), false);
                    if (parameterDetail.getParameter().hasType()
                            && parameterDetail.getParameter().getType().getUnitSetCount() > 0) {
                        for (UnitInfo unitInfo : parameterDetail.getParameter().getType().getUnitSetList()) {
                            stringValue += " " + unitInfo.getUnit();
                        }
                    }
                    if (pval.hasRangeCondition() && pval.getRangeCondition() == RangeCondition.LOW) {
                        return stringValue + " ↓";
                    } else if (pval.hasRangeCondition() && pval.getRangeCondition() == RangeCondition.HIGH) {
                        return stringValue + " ↑";
                    } else {
                        return stringValue;
                    }
                } else {
                    return null;
                }
            }

            @Override
            public Color getForeground(Object element) {
                return JFaceColors.getErrorText(parent.getDisplay());
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    ParameterAlarmData parameterDetail = alarmData.getParameterDetail();
                    switch (parameterDetail.getTriggerValue().getMonitoringResult()) {
                    case IN_LIMITS:
                        return infoIcon;
                    case WATCH:
                        return watchIcon;
                    case WARNING:
                        return warningIcon;
                    case DISTRESS:
                        return distressIcon;
                    case CRITICAL:
                        return criticalIcon;
                    case SEVERE:
                        return severeIcon;
                    case DISABLED:
                        return null;
                    }
                }
                return null;
            }
        });

        TreeViewerColumn typeColumn = new TreeViewerColumn(viewer, SWT.NONE);
        typeColumn.getColumn().setWidth(80);
        typeColumn.getColumn().setText("Alarm Type");
        typeColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    return "Out of Limits";
                } else {
                    return null;
                }
            }
        });

        TreeViewerColumn currentValueColumn = new TreeViewerColumn(viewer, SWT.NONE);
        currentValueColumn.getColumn().setWidth(100);
        currentValueColumn.getColumn().setText("Current Value");
        currentValueColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    ParameterAlarmData parameterDetail = alarmData.getParameterDetail();
                    ParameterValue pval = parameterDetail.getCurrentValue();
                    String stringValue = StringConverter.toString(pval.getEngValue(), false);
                    if (parameterDetail.getParameter().hasType()
                            && parameterDetail.getParameter().getType().getUnitSetCount() > 0) {
                        for (UnitInfo unitInfo : parameterDetail.getParameter().getType().getUnitSetList()) {
                            stringValue += " " + unitInfo.getUnit();
                        }
                    }
                    if (pval.hasRangeCondition() && pval.getRangeCondition() == RangeCondition.LOW) {
                        return stringValue + " ↓";
                    } else if (pval.hasRangeCondition() && pval.getRangeCondition() == RangeCondition.HIGH) {
                        return stringValue + " ↑";
                    } else {
                        return stringValue;
                    }
                } else {
                    return null;
                }
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    ParameterAlarmData parameterDetail = alarmData.getParameterDetail();
                    switch (parameterDetail.getCurrentValue().getMonitoringResult()) {
                    case IN_LIMITS:
                        return infoIcon;
                    case WATCH:
                        return watchIcon;
                    case WARNING:
                        return warningIcon;
                    case DISTRESS:
                        return distressIcon;
                    case CRITICAL:
                        return criticalIcon;
                    case SEVERE:
                        return severeIcon;
                    case DISABLED:
                        return null;
                    }
                }
                return null;
            }
        });

        TreeViewerColumn violationsColumn = new TreeViewerColumn(viewer, SWT.CENTER);
        violationsColumn.getColumn().setWidth(70);
        violationsColumn.getColumn().setText("Violations");
        violationsColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceAlarmNode) {
                    AlarmData alarmData = ((XtceAlarmNode) element).getAlarmData();
                    return String.format("%,d", alarmData.getViolations());
                } else {
                    return null;
                }
            }
        });

        GridLayoutFactory.fillDefaults().generateLayout(parent);

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
        if (!alarmData.hasParameterDetail()) {
            return;
        }

        Display.getDefault().asyncExec(() -> {
            ParameterAlarmData parameterDetail = alarmData.getParameterDetail();
            ParameterValue triggerValue = parameterDetail.getTriggerValue();
            String qname = triggerValue.getId().getName();
            if (!qname.startsWith("/")) {
                throw new IllegalArgumentException("Unexpected id " + qname);
            }
            contentProvider.addElement(qname, alarmData);
            viewer.refresh();
        });
    }

    public void collapseAll() {
        viewer.collapseAll();
    }

    public void expandAll() {
        viewer.expandAll();
    }
}
