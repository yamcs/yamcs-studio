package org.yamcs.studio.links;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.yamcs.client.YamcsClient;
import org.yamcs.studio.core.YamcsPlugin;

public class LinkTableViewer extends TableViewer {

    public static final String COL_NAME = "Name";
    public static final String COL_TYPE = "Type";
    public static final String COL_STATUS = "Status";
    public static final String COL_IN = "In";
    public static final String COL_OUT = "Out";

    public LinkTableViewer(Composite parent, TableColumnLayout tcl) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        setLabelProvider(new LinkTableViewerLabelProvider());

        setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                LinkRecord rec1 = (LinkRecord) o1;
                LinkRecord rec2 = (LinkRecord) o2;
                return rec1.getLinkInfo().getName().compareToIgnoreCase(rec2.getLinkInfo().getName());
            }
        });

        addPopupMenu();
    }

    private void showMessage(Shell shell, String string) {
        MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        dialog.setText("Error");
        dialog.setMessage(string);

        // open dialog and await user selection
        dialog.open();
    }

    private void addFixedColumns(TableColumnLayout tcl) {

        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.LEFT);
        nameColumn.getColumn().setText(COL_NAME);
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(18));

        TableViewerColumn typeColumn = new TableViewerColumn(this, SWT.LEFT);
        typeColumn.getColumn().setText(COL_TYPE);
        tcl.setColumnData(typeColumn.getColumn(), new ColumnWeightData(18));

        TableViewerColumn statusColumn = new TableViewerColumn(this, SWT.CENTER);
        statusColumn.getColumn().setText(COL_STATUS);
        tcl.setColumnData(statusColumn.getColumn(), new ColumnWeightData(18));

        TableViewerColumn inColumn = new TableViewerColumn(this, SWT.RIGHT);
        inColumn.getColumn().setText(COL_IN);
        tcl.setColumnData(inColumn.getColumn(), new ColumnWeightData(10));

        TableViewerColumn outColumn = new TableViewerColumn(this, SWT.RIGHT);
        outColumn.getColumn().setText(COL_OUT);
        tcl.setColumnData(outColumn.getColumn(), new ColumnWeightData(10));

        // Common properties to all columns
        List<TableViewerColumn> columns = new ArrayList<>();
        columns.add(nameColumn);
        columns.add(typeColumn);
        columns.add(statusColumn);
        columns.add(inColumn);
        columns.add(outColumn);
        for (TableViewerColumn column : columns) {
            // prevent resize to 0
            column.getColumn().addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (column.getColumn().getWidth() < 5) {
                        column.getColumn().setWidth(5);
                    }
                }
            });
        }
    }

    private void addPopupMenu() {
        Menu contextMenu = new Menu(getTable());
        getTable().setMenu(contextMenu);
        MenuItem mItem1 = new MenuItem(contextMenu, SWT.None);
        mItem1.setText("Enable Link");
        mItem1.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                LinkRecord rec = (LinkRecord) (getTable().getSelection()[0].getData());
                if (rec == null) {
                    return;
                }

                YamcsClient yamcs = YamcsPlugin.getYamcsClient();
                yamcs.enableLink(rec.getLinkInfo().getInstance(), rec.getLinkInfo().getName())
                        .whenComplete((data, exc) -> {
                            if (exc != null) {
                                getTable().getDisplay().asyncExec(() -> {
                                    showMessage(getTable().getShell(), exc.getMessage());
                                });
                            }
                        });
            }
        });

        MenuItem mItem2 = new MenuItem(contextMenu, SWT.None);
        mItem2.setText("Disable Link");
        mItem2.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                LinkRecord rec = (LinkRecord) (getTable().getSelection()[0].getData());
                if (rec == null) {
                    return;
                }

                YamcsClient yamcs = YamcsPlugin.getYamcsClient();
                yamcs.disableLink(rec.getLinkInfo().getInstance(), rec.getLinkInfo().getName())
                        .whenComplete((data, exc) -> {
                            if (exc != null) {
                                getTable().getDisplay().asyncExec(() -> {
                                    showMessage(getTable().getShell(), exc.getMessage());
                                });
                            }
                        });
            }
        });
    }
}
