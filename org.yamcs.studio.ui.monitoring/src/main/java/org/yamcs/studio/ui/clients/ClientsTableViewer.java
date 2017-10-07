package org.yamcs.studio.ui.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;

public class ClientsTableViewer extends TableViewer {

    private static final Logger log = Logger.getLogger(ClientsTableViewer.class.getName());

    public static final String COL_ID = "ID";
    public static final String COL_USER = "User";
    public static final String COL_APPLICATION = "Application";
    public static final String COL_INSTANCE = "Instance";
    public static final String COL_PROCESSOR = "Processor";

    private ClientsContentProvider contentProvider;
    private ClientsView clientsView;
    Composite parent;

    public ClientsTableViewer(ClientsView clientsView, Composite parent, TableColumnLayout tcl) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));
        this.clientsView = clientsView;
        this.parent = parent;

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        setLabelProvider(new ClientLabelProvider());

        // Does it make sense to have a menu to change processor for a client, as in yamcs-monitor ?
        // Since yamcs-studio has its own control to switch processor for itself.

        //        // add popup menu
        //        Menu contextMenu = new Menu(getTable());
        //        getTable().setMenu(contextMenu);
        //        MenuItem mItem1 = new MenuItem(contextMenu, SWT.None);
        // ...
    }

    private void addFixedColumns(TableColumnLayout tcl) {

        TableViewerColumn aColumn = new TableViewerColumn(this, SWT.CENTER);
        aColumn.getColumn().setText(COL_ID);
        tcl.setColumnData(aColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn bColumn = new TableViewerColumn(this, SWT.LEFT);
        bColumn.getColumn().setText(COL_USER);
        tcl.setColumnData(bColumn.getColumn(), new ColumnWeightData(23));

        TableViewerColumn cColumn = new TableViewerColumn(this, SWT.LEFT);
        cColumn.getColumn().setText(COL_APPLICATION);
        tcl.setColumnData(cColumn.getColumn(), new ColumnWeightData(23));

        TableViewerColumn dColumn = new TableViewerColumn(this, SWT.LEFT);
        dColumn.getColumn().setText(COL_INSTANCE);
        tcl.setColumnData(dColumn.getColumn(), new ColumnWeightData(23));

        TableViewerColumn eColumn = new TableViewerColumn(this, SWT.LEFT);
        eColumn.getColumn().setText(COL_PROCESSOR);
        tcl.setColumnData(eColumn.getColumn(), new ColumnWeightData(23));

        // Common properties to all columns
        List<TableViewerColumn> columns = new ArrayList<>();
        columns.add(aColumn);
        columns.add(bColumn);
        columns.add(cColumn);
        columns.add(dColumn);
        columns.add(eColumn);
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

    class ClientLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            // no image to show
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            // each element comes from the ContentProvider.getElements(Object)
            if (!(element instanceof ClientInfo)) {
                return "";
            }
            ClientInfo model = (ClientInfo) element;
            switch (columnIndex) {
            case 0:
                return model.getId() + "";
            case 1:
                return model.getUsername();
            case 2:
                return model.getApplicationName();
            case 3:
                return model.getInstance();
            case 4:
                return model.getProcessorName();
            default:
                break;
            }
            return "";
        }
    }
}
