package org.yamcs.studio.commanding.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Rest.EditCommandQueueEntryRequest;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.security.YamcsAuthorizations;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class CommandQueuedTableViewer extends TableViewer {

    private static final Logger log = Logger.getLogger(CommandQueuedTableViewer.class.getName());

    public static final String COL_QUEUE = "Queue";
    public static final String COL_USER = "User";
    public static final String COL_STRING = "Command String";
    public static final String COL_TIME = "Time";

    public CommandQueuedTableViewer(CommandQueueView commandQueueView, Composite parent, TableColumnLayout tcl) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        setLabelProvider(new CqeLabelProvider());

        // add popup menu
        Menu contextMenu = new Menu(getTable());
        getTable().setMenu(contextMenu);
        MenuItem mItem1 = new MenuItem(contextMenu, SWT.None);
        mItem1.setText("Send");
        mItem1.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                CommandQueueEntry cqe = (CommandQueueEntry) (getTable().getSelection()[0].getData());
                if (cqe == null) {
                    return;
                }
                long missionTime = TimeCatalogue.getInstance().getMissionTime();
                long timeinthequeue = missionTime - cqe.getGenerationTime();
                if (timeinthequeue > CommandQueueView.oldCommandWarningTime * 1000L) {
                    int res = CommandFateDialog.showDialog(parent.getShell(), cqe.getCmdId());
                    switch (res) {
                    case -1: // cancel
                        return;
                    case 0: // rebuild the command
                        log.info("sending command with updated time: " + cqe.getSource());
                        updateQueueEntryState(cqe, "released", true);
                        break;
                    case 1: // send the command with the old generation time
                        log.info("sending command: " + cqe);
                        updateQueueEntryState(cqe, "released", false);
                        break;
                    case 2: // rejecting command
                        log.info("rejecting command: " + cqe.getSource());
                        updateQueueEntryState(cqe, "rejected", false);
                    }
                } else {
                    log.info("sending command: " + cqe.getSource());
                    updateQueueEntryState(cqe, "released", false);
                }
            }

        });
        MenuItem mItem2 = new MenuItem(contextMenu, SWT.None);
        mItem2.setText("Reject");
        mItem2.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                CommandQueueEntry cqe = (CommandQueueEntry) (getTable().getSelection()[0].getData());
                if (cqe == null) {
                    return;
                }
                log.info("rejecting command: " + cqe.getSource());
                updateQueueEntryState(cqe, "rejected", false);
            }

        });
        getTable().addListener(SWT.MouseDown, event -> {
            TableItem[] selection = getTable().getSelection();
            if (selection.length != 0 && (event.button == 3)) {
                if (YamcsAuthorizations.getInstance().hasSystemPrivilege(YamcsAuthorizations.ControlCommandQueue)) {
                    contextMenu.setVisible(true);
                } else {
                    contextMenu.setVisible(false);
                }
            }
        });
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_QUEUE);
        nameColumn.getColumn().setToolTipText("The queue which contains the command");
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn userColumn = new TableViewerColumn(this, SWT.NONE);
        userColumn.getColumn().setText(COL_USER);
        userColumn.getColumn().setToolTipText("The user who submitted the command");
        tcl.setColumnData(userColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn stringColumn = new TableViewerColumn(this, SWT.CENTER);
        stringColumn.getColumn().setText(COL_STRING);
        stringColumn.getColumn().setToolTipText("Command source code");
        tcl.setColumnData(stringColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn timeColumn = new TableViewerColumn(this, SWT.LEFT);
        timeColumn.getColumn().setText(COL_TIME);
        timeColumn.getColumn().setToolTipText("Time");
        tcl.setColumnData(timeColumn.getColumn(), new ColumnWeightData(200));

        // Common properties to all columns
        List<TableViewerColumn> columns = new ArrayList<>();
        columns.add(nameColumn);
        columns.add(userColumn);
        columns.add(stringColumn);
        columns.add(timeColumn);
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

    // rebuild doesn't seem to do anything and therefore is not currently included in rest api
    // keeping it around for future reference mostly
    private void updateQueueEntryState(CommandQueueEntry entry, String state, boolean rebuild) {
        EditCommandQueueEntryRequest req = EditCommandQueueEntryRequest.newBuilder().setState(state).build();
        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        catalogue.editQueuedCommand(entry, req);
    }

    // Command Queue Entry label provider
    class CqeLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            // no image to show
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            // each element comes from the ContentProvider.getElements(Object)
            if (!(element instanceof CommandQueueEntry)) {
                return "";
            }
            CommandQueueEntry model = (CommandQueueEntry) element;
            switch (columnIndex) {
            case 0:
                return model.getQueueName();
            case 1:
                return model.getUsername();
            case 2:
                return model.getSource();
            case 3:
                return YamcsUIPlugin.getDefault().formatInstant(model.getGenerationTime());
            default:
                break;
            }
            return "";
        }
    }

}
