package org.yamcs.studio.displays;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

import com.google.protobuf.Timestamp;

public class ScrollViewer extends TableViewer {

    public static final String COL_TIME = "Timestamp";

    private final int MAX_SIZE = 100;

    public static final String ENG = "ENG";
    public static final String RAW = "RAW";

    private String valueType = ENG;

    private ScrollParameterContentProvider contentProvider;
    private TableColumnLayout tcl;
    private List<String> parameters = new ArrayList<>();
    private List<String> qualifiedNames = new ArrayList<>();

    private List<Listener> listeners = new ArrayList<>();

    public ScrollViewer(Composite parent) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL));
        tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);

        contentProvider = new ScrollParameterContentProvider();
        setContentProvider(contentProvider);
        setInput(contentProvider);

        TableViewerColumn timeColumn = new TableViewerColumn(this, SWT.LEFT);
        timeColumn.getColumn().setText(COL_TIME);
        tcl.setColumnData(timeColumn.getColumn(), new ColumnWeightData(30));
        timeColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ParameterData pdata = (ParameterData) element;
                ParameterValue pval = pdata.getParameter(0);
                Timestamp proto = pval.getGenerationTime();
                Instant generationTime = Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos());
                return YamcsUIPlugin.getDefault().formatInstant(generationTime);
            }
        });
    }

    /*private void addColumn(ParameterInfo info) {
    
        ParameterCatalogue.getInstance().subscribeParameters(NamedObjectList.newBuilder()
                .addList(info.getAlias(0)).build());
        TableViewerColumn column = new TableViewerColumn(this, SWT.RIGHT);
        column.getColumn().setText(info.getName());
        column.getColumn().setToolTipText(info.getQualifiedName());
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(20));
        column.setLabelProvider(new ColumnLabelProvider() {
    
            @Override
            public String getText(Object element) {
                ParameterData data = (ParameterData) element;
                for (ParameterValue value : data.getParameterList()) {
                    if (value.getId().getName().equals(info.getName())) {
                        if (valueType.equals(ENG)) {
                            return StringConverter.toString(value.getEngValue());
                        }
                        return StringConverter.toString(value.getRawValue());
    
                    }
                }
                return "-";
            }
        });
    }*/

    public void addParameter(ParameterInfo element) {
        if (parameters.contains(element.getName())) {
            return;
        }
        parameters.add(element.getName());
        qualifiedNames.add(element.getQualifiedName());
        // addColumn(element);
        getTable().getColumn(0).setWidth(180);
        for (int i = 1; i < getTable().getColumnCount(); i++) {
            getTable().getColumn(i).setWidth(60);
        }

        for (Listener l : listeners) {
            l.handleEvent(new Event());
        }

        refresh();
    }

    public void removeParameter(String info) {
        int i;
        for (i = 1; i < getTable().getColumnCount(); i++) {
            if (getTable().getColumn(i).getText().equals(info)) {
                break;
            }
        }

        getTable().getColumn(i).dispose();
        parameters.remove(info);
        String qualifiedName = "";
        for (String qname : qualifiedNames) {
            if (qname.endsWith(info)) {
                qualifiedName = qname;
                break;
            }
        }
        qualifiedNames.remove(qualifiedName);
        for (Listener l : listeners) {
            l.handleEvent(new Event());
        }
        refresh();
    }

    public void clear() {
        while (getTable().getColumnCount() > 1) {
            getTable().getColumns()[1].dispose();
        }

        parameters.clear();
        qualifiedNames.clear();
        contentProvider.clearAll();
        for (Listener l : listeners) {
            l.handleEvent(new Event());
        }
        refresh();
    }

    public List<String> getParameters() {
        return qualifiedNames;
    }

    public void addDataChangedListener(Listener listener) {
        listeners.add(listener);
    }

    public class ScrollParameterContentProvider implements IStructuredContentProvider {

        private List<ParameterData> values = new ArrayList<>();

        public void clearAll() {
            values.clear();
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return values.toArray();
        }

        private boolean hasData(ParameterData data) {
            for (ParameterValue v : data.getParameterList()) {
                if (parameters.contains(v.getId().getName())) {
                    return true;
                }
            }
            return false;
        }

        public void addParameterData(ParameterData data) {
            if (hasData(data)) {
                values.add(0, data);
                if (values.size() > MAX_SIZE) {
                    values.remove(MAX_SIZE);
                }
            }

            if (getTable().isDisposed()) {
                return;
            }
            Display.getDefault().asyncExec(() -> {
                if (!getTable().isDisposed()) {
                    ScrollViewer.this.refresh();
                }
            });
        }
    }

    public void onParameterData(ParameterData pdata) {
        contentProvider.addParameterData(pdata);
    }

    public void setValue(String string) {
        valueType = string;
    }

    public String getValue() {
        return valueType;
    }
}
