package org.yamcs.studio.displays;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.StringConverter;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

import com.google.protobuf.Timestamp;

public class ParameterTableViewer extends TableViewer {

    public static final String COL_NAME = "Parameter";
    public static final String COL_ENG = "Eng";
    public static final String COL_RAW = "Raw";
    public static final String COL_GENERATION_TIME = "Generation time";
    public static final String COL_RECEPTION_TIME = "Reception time";

    private ParameterTableContentProvider contentProvider;
    private Map<String, ParameterValue> values = new HashMap<>();
    private TableColumnLayout tcl;

    private Image normalIcon;
    private Image watchIcon;
    private Image warningIcon;
    private Image distressIcon;
    private Image criticalIcon;
    private Image severeIcon;

    public ParameterTableViewer(Composite parent) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL));
        tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        normalIcon = resourceManager.createImage(getImageDescriptor("icons/eview/level0s.png"));
        watchIcon = resourceManager.createImage(getImageDescriptor("icons/eview/level1s.png"));
        warningIcon = resourceManager.createImage(getImageDescriptor("icons/eview/level2s.png"));
        distressIcon = resourceManager.createImage(getImageDescriptor("icons/eview/level3s.png"));
        criticalIcon = resourceManager.createImage(getImageDescriptor("icons/eview/level4s.png"));
        severeIcon = resourceManager.createImage(getImageDescriptor("icons/eview/level5s.png"));

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);

        contentProvider = new ParameterTableContentProvider(this);
        setContentProvider(contentProvider);
        setInput(contentProvider);

        TableViewerColumn column = new TableViewerColumn(this, SWT.LEFT);
        column.getColumn().setText(COL_NAME);
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(40));
        column.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return (String) element;
            }

            @Override
            public Image getImage(Object element) {
                ParameterValue value = values.get(element);
                if (value == null || !value.hasMonitoringResult()) {
                    return null;
                }
                switch (value.getMonitoringResult()) {
                case IN_LIMITS:
                    return normalIcon;
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
                default:
                    return null;
                }
            }
        });

        column = new TableViewerColumn(this, SWT.LEFT);
        column.getColumn().setText(COL_ENG);
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(40));
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ParameterValue value = values.get(element);
                if (value == null) {
                    return "-";
                }
                return StringConverter.toString(value.getEngValue());
            }
        });

        column = new TableViewerColumn(this, SWT.LEFT);
        column.getColumn().setText(COL_RAW);
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(40));
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ParameterValue value = values.get(element);
                if (value == null) {
                    return "-";
                }
                return StringConverter.toString(value.getRawValue());
            }
        });

        column = new TableViewerColumn(this, SWT.LEFT);
        column.getColumn().setText(COL_GENERATION_TIME);
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(40));
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ParameterValue value = values.get(element);
                if (value == null) {
                    return "-";
                }
                Timestamp proto = value.getGenerationTime();
                Instant generationTime = Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos());
                return YamcsUIPlugin.getDefault().formatInstant(generationTime);
            }
        });

        column = new TableViewerColumn(this, SWT.LEFT);
        column.getColumn().setText(COL_RECEPTION_TIME);
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(40));
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ParameterValue value = values.get(element);
                if (value == null) {
                    return "-";
                }
                Timestamp proto = value.getAcquisitionTime();
                Instant receptionTime = Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos());
                return YamcsUIPlugin.getDefault().formatInstant(receptionTime);
            }
        });
    }

    private ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor.createFromURL(FileLocator
                .find(Platform.getBundle("org.yamcs.studio.displays"), new Path(path), null));
    }

    /*public void attachParameterInfo(ParameterInfo info) {
        NamedObjectList list = NamedObjectList.newBuilder()
                .addList(info.getAlias(0)).build();
        ParameterCatalogue.getInstance().subscribeParameters(list);
        if (contentProvider.addParameter(info.getQualifiedName())) {
            values.put(info.getQualifiedName(), null);
        }
        refresh();
    }*/

    public void removeParameter(String info) {
        values.remove(info);
        contentProvider.remove(info);
        refresh();
    }

    public void clear() {
        values.clear();
        contentProvider.clearAll();
        refresh();
    }

    public List<String> getParameters() {
        return contentProvider.getParameters();
    }

    public boolean hasChanged() {
        return contentProvider.hasChanged();
    }

    public void onParameterData(ParameterData pdata) {
        for (ParameterValue value : pdata.getParameterList()) {
            if (values.keySet().contains(value.getId().getNamespace() + "/" + value.getId().getName())) {
                values.put(value.getId().getNamespace() + "/" + value.getId().getName(), value);

            }
        }
        if (getTable().isDisposed()) {
            return;
        }
        Display.getDefault().asyncExec(() -> {
            if (!getTable().isDisposed()) {
                ParameterTableViewer.this.refresh();
            }
        });
    }
}
