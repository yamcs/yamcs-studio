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
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.pvmanager.PVConnectionInfo;
import org.yamcs.studio.core.pvmanager.YamcsPVReader;

public class ParameterTableViewer extends TableViewer {

    public static final String COL_CONTAINER = "Container";
    public static final String COL_NAME = "Parameter";
    public static final String COL_ENG = "Eng Value";
    public static final String COL_RAW = "Raw Value";
    
    private Map<ParameterInfo, String> engValue;
    private Map<ParameterInfo, String> rawValue;
    ParameterContentProvider contentProvider;
	
	
	public ParameterTableViewer(AlphaNumericView view, Composite parent, TableColumnLayout tcl) {
		super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));;
        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        engValue = new HashMap<>();
        rawValue = new HashMap<>();
        addFixedColumns(tcl);
        contentProvider = new ParameterContentProvider(this);
        setContentProvider(contentProvider);
        setInput(contentProvider);
        
	}
	
    private void addFixedColumns(TableColumnLayout tcl) {

        TableViewerColumn containerColumn = new TableViewerColumn(this, SWT.NONE);
        containerColumn.getColumn().setText(COL_CONTAINER);
        tcl.setColumnData(containerColumn.getColumn(), new ColumnWeightData(40));
        containerColumn.setLabelProvider(new ColumnLabelProvider() {           
        	
        	@Override 
            public String getText(Object element) {
            	ParameterInfo cnt = (ParameterInfo) element;
                System.out.println("Adding value " + cnt.getName());
                return cnt.getName();
            }
        });
        

        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.CENTER);
        nameColumn.getColumn().setText(COL_NAME);
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(10));
        nameColumn.setLabelProvider(new ColumnLabelProvider() {           
        	
        	@Override 
            public String getText(Object element) {
            	ParameterInfo cnt = (ParameterInfo) element;
                          
                return cnt.getQualifiedName();
            }
        });

        TableViewerColumn engValueColumn = new TableViewerColumn(this, SWT.CENTER);
        engValueColumn.getColumn().setText(COL_ENG);
        tcl.setColumnData(engValueColumn.getColumn(), new ColumnWeightData(10));
        engValueColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override 
            public String getText(Object element) {
            	System.out.println("Eng:" + engValue.get(element));
                return engValue.get(element);
            }
        });

        TableViewerColumn rawValueColumn = new TableViewerColumn(this, SWT.CENTER);
        rawValueColumn.getColumn().setText(COL_RAW);
        tcl.setColumnData(rawValueColumn.getColumn(), new ColumnWeightData(10));
        rawValueColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override 
            public String getText(Object element) {
            	return rawValue.get(element);
            }
        });

        // Common properties to all columns
        List<TableViewerColumn> columns = new ArrayList<>();
        columns.add(containerColumn);
        columns.add(nameColumn);
        columns.add(engValueColumn);
        columns.add(rawValueColumn);
        for (TableViewerColumn column : columns) {
            // prevent resize to 0
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

    }
    

    public void addParameter(ParameterInfo element) {
    	ParameterCatalogue.getInstance().register(new ParameterReader(element));
    	contentProvider.addParameter(element);

    }
    
    
    
    class ParameterReader implements YamcsPVReader{

    	NamedObjectId id;
    	ParameterInfo info;
    	
    	public ParameterReader(ParameterInfo info) {
    		for(ParameterInfo parameter: ParameterCatalogue.getInstance().getMetaParameters()) {
    			if(info.getQualifiedName().equals(parameter.getQualifiedName())) {
    				id = parameter.getAliasList().get(0);
    			}
    		}
		}
    	
		@Override
		public void reportException(Exception e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public NamedObjectId getId() {
			return id;
		}

		@Override
		public void processConnectionInfo(PVConnectionInfo info) {
			if(!info.connected) {
				//TODO do something
			}
							
		}

		@Override
		public void processParameterValue(ParameterValue pval) {
			engValue.put(info, pval.getEngValue().toString());
			rawValue.put(info, pval.getRawValue().toString());
			System.out.println("Updating eng:" + pval.getEngValue().toString());
			ParameterTableViewer.this.refresh();
			
		}
    	
    }
    
    class ParameterContentProvider implements IStructuredContentProvider {

    	TableViewer table;
    	List<ParameterInfo> parameter;
    	
    	
		public ParameterContentProvider(TableViewer parameterTableViewer) {
			table = parameterTableViewer;
			parameter = new ArrayList<>();
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return parameter.toArray();
		}
		
		public void addParameter(ParameterInfo info) {
			parameter.add(info);
			table.add(info);
		}
    	
    }


}
