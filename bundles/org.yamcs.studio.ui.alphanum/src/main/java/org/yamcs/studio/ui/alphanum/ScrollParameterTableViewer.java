package org.yamcs.studio.ui.alphanum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.ParameterListener;

public class ScrollParameterTableViewer extends TableViewer implements ParameterListener {

    public static final String COL_TIME = "Timestamp";

    private final int MAX_SIZE = 100;

    private final String ENG = "ENG";
    private final String RAW = "RAW";
    
    private String valueType = ENG;
    
    private ScrollParameterContentProvider contentProvider;
    private TableColumnLayout tcl;
    private List<String> parameters;
    private List<String> qualifiedNames;

    public ScrollParameterTableViewer(Composite parent) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL));;
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
                ParameterData data = (ParameterData) element;
                Date date = new Date(data.getParameter(0).getGenerationTime());
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                return sdfDate.format(date);
            }
        });

        timeColumn.getColumn().addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {
            }

            @Override
            public void controlResized(ControlEvent e) {
                if (timeColumn.getColumn().getWidth() < 5)
                    timeColumn.getColumn().setWidth(5);
            }
        });
        parameters = new ArrayList<>();
        qualifiedNames = new ArrayList<>();

        ParameterCatalogue.getInstance().addParameterListener(this);

    }

    private void addColumn(ParameterInfo info) {

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
                for( ParameterValue value : data.getParameterList())
                    if(value.getId().getName().equals(info.getName())){
                        if(valueType.equals(ENG))
                            return String.valueOf(getValue(value.getEngValue()));
                        return String.valueOf(getValue(value.getRawValue()));
                            
                    }                
                return "-";
            }
        });
        column.getColumn().addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {

            }

            @Override
            public void controlResized(ControlEvent e) {
                if (column.getColumn().getWidth() < 40)
                    column.getColumn().setWidth(40);
            }
        });       

    }


    public void addParameter(ParameterInfo element) {
        if(parameters.contains(element.getName()))
            return;
        parameters.add(element.getName());
        qualifiedNames.add(element.getQualifiedName());
        addColumn(element);
        getTable().getColumn(0).setWidth(180);
        for(int i = 1; i < getTable().getColumnCount(); i ++) {
            getTable().getColumn(i).setWidth(60);
        }
        refresh();
    }

    public void removeParameter(ParameterInfo info) {
        int i;
        for(i = 1; i < getTable().getColumnCount(); i ++) {
            if(getTable().getColumn(i).getText().equals(info.getName()))
                break;
        }
        
        getTable().getColumn(i).dispose();
        parameters.remove(info.getName());
        qualifiedNames.remove(info.getQualifiedName());
        refresh();
    }

    public void clear() {
        while ( getTable().getColumnCount() > 1 ) 
            getTable().getColumns()[1].dispose();

        parameters.clear();
        qualifiedNames.clear();
        contentProvider.clearAll();
        refresh();

    }

    public List<String> getParameters() {
        return qualifiedNames;
    }
    
    private Object getValue(Value value) {
        Object obj = null;
        if(value.hasStringValue())
            obj= value.getStringValue();
        else if(value.hasSint64Value())
            obj= value.getSint64Value();
        else if(value.hasSint32Value()) 
            obj= value.getSint32Value();
        else if(value.hasUint64Value())
            obj= value.getUint64Value();
        else if(value.hasUint32Value()) 
            obj= value.getUint32Value();
        else if(value.hasDoubleValue())
            obj= value.getDoubleValue();
        else if(value.hasFloatValue())
            obj= value.getFloatValue();
        else if(value.hasBooleanValue())
            obj= value.getBooleanValue();
        if(obj == null)
            return "-";
        return obj;
    }


    public class ScrollParameterContentProvider implements IStructuredContentProvider {

        private List<ParameterData> values;


        public ScrollParameterContentProvider() {
            values = new ArrayList<>();
        }

        public void clearAll() {
            values.clear();
            
        }

        @Override
        public void dispose() {


        }
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub

        }

        @Override
        public Object[] getElements(Object inputElement) {
            return values.toArray();
        }


        private boolean hasData(ParameterData data) {

            for(ParameterValue v: data.getParameterList()) {
                if (parameters.contains(v.getId().getName()))
                    return true;
            }
            return false;
        }


        public void addParameterData(ParameterData data) {
            if(hasData(data)) {
                values.add(0, data);
                if(values.size() > MAX_SIZE) {
                    values.remove(MAX_SIZE);
                }
            }

            if (getTable().isDisposed()) {
                return;
            }
            Display.getDefault().asyncExec( () -> {
                if (!getTable().isDisposed()) {
                    ScrollParameterTableViewer.this.refresh();
                }
            });
        }


    }


    @Override
    public void mdbUpdated() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onParameterData(ParameterData pdata) {
        contentProvider.addParameterData(pdata);

    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        // TODO Auto-generated method stub

    }

    public void setValue(String string) {
        valueType = string;
        
    }


}
