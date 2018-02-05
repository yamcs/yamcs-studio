package org.yamcs.studio.ui.alphanum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.pvmanager.PVConnectionInfo;
import org.yamcs.studio.core.pvmanager.YamcsPVReader;

public class ScrollParameterTableViewer extends TableViewer {

    public static final String COL_TIME = "Timestamp";
    
    private final int MAX_SIZE = 100;

    private ScrollParameterContentProvider contentProvider;
    private Map<ParameterInfo, ParameterReader> readers;
    private TableColumnLayout tcl;

    public ScrollParameterTableViewer(Composite parent) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL));;
        tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        readers = new HashMap<>();
        contentProvider = new ScrollParameterContentProvider(this);
        setContentProvider(contentProvider);
        setInput(contentProvider);


        TableViewerColumn timeColumn = new TableViewerColumn(this, SWT.LEFT);
        timeColumn.getColumn().setText(COL_TIME);
        tcl.setColumnData(timeColumn.getColumn(), new ColumnWeightData(80));
        timeColumn.setLabelProvider(new ColumnLabelProvider() {           

            @Override 
            public String getText(Object element) {
                String cnt = (String) element;
                return cnt.replace("Z", "").replace("T", " ");
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

    }

    private void addColumn(ParameterInfo info) {
        TableViewerColumn column = new TableViewerColumn(this, SWT.RIGHT);
        column.getColumn().setText(info.getName());
        column.getColumn().setToolTipText(info.getQualifiedName());
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(40));
        ParameterReader reader = new ParameterReader(info);
        ParameterCatalogue.getInstance().register(reader);
        readers.put(info, reader);
        column.setLabelProvider(new ColumnLabelProvider() {           

            @Override 
            public String getText(Object element) {
                return reader.getValue((String)element);
            }
        });
        column.getColumn().addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {
                
            }

            @Override
            public void controlResized(ControlEvent e) {
                if (column.getColumn().getWidth() < 5)
                    column.getColumn().setWidth(5);
            }
        });       

    }


    public void addParameter(ParameterInfo element) {
        addColumn(element);
    }

//    public void removeParameter(ParameterInfo info) {
//        ParameterCatalogue.getInstance().unregister(readers.get(info));
//        readers.remove(info);
//        contentProvider.remove(info);
//        refresh();
//    }
//
//    public void restoreParameters() {
//        clear();
//        for(ParameterInfo info : contentProvider.getInitial()) {
//            addParameter(info);
//        }
//        refresh();
//
//    }
//
//    public void clear() {
//        for(ParameterInfo info : readers.keySet()) {
//            ParameterCatalogue.getInstance().unregister(readers.get(info));
//        }
//        readers.clear();
//        contentProvider.clearAll();
//        refresh();
//
//    }
//
//    public List<ParameterInfo> getParameters() {
//        return contentProvider.getParameter();
//    }
//
//    public boolean hasChanged() {
//        return contentProvider.hasChanged();
//    }


    class ParameterReader implements YamcsPVReader{

        NamedObjectId id;
        Map<String, String> value;

        public ParameterReader(ParameterInfo info) {
            id = info.getAliasList().get(0);
            value = new HashMap<>();
        }

        @Override
        public void reportException(Exception e) {
            ;

        }

        @Override
        public NamedObjectId getId() {
            return id;
        }

        public String getValue(String timeStamp) {
            if(value.get(timeStamp) == null) {
                return "-";
            }
            return value.get(timeStamp);
        }

        @Override
        public void processConnectionInfo(PVConnectionInfo info) {
            if(!info.connected) {
                //TODO do something
            }		
        }

        @Override
        public void processParameterValue(ParameterValue pval) {
            value.put(pval.getGenerationTimeUTC(), extractValue(pval.getEngValue()));
            contentProvider.addTimeStamp(pval.getGenerationTimeUTC());
            if (getTable().isDisposed()) {
                return;
            }
            Display.getDefault().asyncExec( () -> {
                if (!getTable().isDisposed()) {
                    ScrollParameterTableViewer.this.refresh();
                }
            });

        }
        public void remove(String removed) {
            value.remove(removed);
            
        }
        
        private String extractValue(Value value) {
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
            return String.valueOf(obj);
        }

    }


    public class ScrollParameterContentProvider implements IStructuredContentProvider {

        TableViewer table;
        List<String> timestamps;
        

        public ScrollParameterContentProvider(TableViewer parameterTableViewer) {
            table = parameterTableViewer;
            timestamps = new ArrayList<>();
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
            return timestamps.toArray();
        }

        public boolean addTimeStamp(String timestamp) {
            if(timestamps.contains(timestamp))
                return false;
            timestamps.add(0, timestamp);
            if(timestamps.size() > MAX_SIZE) {
                
                String removed = timestamps.remove(MAX_SIZE);
                for(ParameterInfo info : readers.keySet()) {
                    readers.get(info).remove(removed);
                }
            }
            return true;
        }

    }






}
