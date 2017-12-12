package org.yamcs.studio.monitoring.clients;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;

public class ClientsTableViewer extends TableViewer {

    public static final String COL_ID = "ID";
    public static final String COL_USER = "Username";
    public static final String COL_APPLICATION = "Application";
    public static final String COL_INSTANCE = "Instance";
    public static final String COL_PROCESSOR = "Processor";

    public ClientsTableViewer(Composite parent) {
        super(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns();
    }

    private void addFixedColumns() {

        TableViewerColumn idColumn = new TableViewerColumn(this, SWT.CENTER);
        idColumn.getColumn().setText(COL_ID);
        idColumn.getColumn().setWidth(30);
        idColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ClientInfo client = (ClientInfo) element;
                return String.valueOf(client.getId());
            }
        });

        TableViewerColumn userColumn = new TableViewerColumn(this, SWT.LEFT);
        userColumn.getColumn().setText(COL_USER);
        userColumn.getColumn().setWidth(100);
        userColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ClientInfo client = (ClientInfo) element;
                return String.valueOf(client.getUsername());
            }
        });

        TableViewerColumn applicationColumn = new TableViewerColumn(this, SWT.LEFT);
        applicationColumn.getColumn().setText(COL_APPLICATION);
        applicationColumn.getColumn().setWidth(100);
        applicationColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ClientInfo client = (ClientInfo) element;
                return client.getApplicationName();
            }
        });

        TableViewerColumn instanceColumn = new TableViewerColumn(this, SWT.LEFT);
        instanceColumn.getColumn().setText(COL_INSTANCE);
        instanceColumn.getColumn().setWidth(100);
        instanceColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ClientInfo client = (ClientInfo) element;
                return client.getInstance();
            }
        });

        TableViewerColumn processorColumn = new TableViewerColumn(this, SWT.LEFT);
        processorColumn.getColumn().setText(COL_PROCESSOR);
        processorColumn.getColumn().setWidth(100);
        processorColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ClientInfo client = (ClientInfo) element;
                return client.getProcessorName();
            }
        });

        // prevent resize to 0
        for (TableColumn column : getTable().getColumns()) {
            column.addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (column.getWidth() < 5) {
                        column.setWidth(5);
                    }
                }
            });
        }
    }
}
