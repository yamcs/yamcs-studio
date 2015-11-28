package org.yamcs.studio.ui.links;

import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.YamcsManagement.LinkInfo;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.ui.utils.UiColors;
import org.yamcs.studio.core.web.ResponseHandler;

import com.google.protobuf.MessageLite;

public class LinksTableViewer extends TableViewer {

    public static final String COL_NAME = "Name";
    public static final String COL_TYPE = "Type";
    public static final String COL_SPEC = "Spec";
    public static final String COL_STREAM = "Stream";
    public static final String COL_STATUS = "Status";
    public static final String COL_DATACOUNT = "Data Count";

    private LinksView linksView;
    Composite parent;

    public LinksTableViewer(LinksView linksView, Composite parent, TableColumnLayout tcl) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));
        this.linksView = linksView;
        this.parent = parent;

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        setLabelProvider(new LinkLabelProvider());

        // add popup menu
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
                LinkInfo li = (LinkInfo) (getTable().getSelection()[0].getData());
                if (li == null)
                    return;

                LinkCatalogue catalogue = LinkCatalogue.getInstance();
                catalogue.enableLink(li.getInstance(), li.getName(), new ResponseHandler() {

                    @Override
                    public void onMessage(MessageLite responseMsg) {
                        // success
                    }

                    @Override
                    public void onException(Exception e) {
                        getTable().getDisplay().asyncExec(() -> {
                            showMessage(parent.getShell(), e.getMessage());
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
                LinkInfo li = (LinkInfo) (getTable().getSelection()[0].getData());
                if (li == null)
                    return;

                LinkCatalogue catalogue = LinkCatalogue.getInstance();
                catalogue.disableLink(li.getInstance(), li.getName(), new ResponseHandler() {

                    @Override
                    public void onMessage(MessageLite responseMsg) {
                        // success
                    }

                    @Override
                    public void onException(Exception e) {
                        getTable().getDisplay().asyncExec(() -> {
                            showMessage(parent.getShell(), e.getMessage());
                        });
                    }
                });
            }
        });

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

        TableViewerColumn specColumn = new TableViewerColumn(this, SWT.LEFT);
        specColumn.getColumn().setText(COL_SPEC);
        tcl.setColumnData(specColumn.getColumn(), new ColumnWeightData(18));

        TableViewerColumn streamColumn = new TableViewerColumn(this, SWT.LEFT);
        streamColumn.getColumn().setText(COL_STREAM);
        tcl.setColumnData(streamColumn.getColumn(), new ColumnWeightData(18));

        TableViewerColumn statusColumn = new TableViewerColumn(this, SWT.CENTER);
        statusColumn.getColumn().setText(COL_STATUS);
        tcl.setColumnData(statusColumn.getColumn(), new ColumnWeightData(18));

        TableViewerColumn datacount = new TableViewerColumn(this, SWT.RIGHT);
        datacount.getColumn().setText(COL_DATACOUNT);
        tcl.setColumnData(datacount.getColumn(), new ColumnWeightData(10));
    }

    class LinkLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

        private final NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            // no image to show
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            // each element comes from the ContentProvider.getElements(Object)
            if (!(element instanceof LinkInfo)) {
                return "";
            }
            LinkInfo model = (LinkInfo) element;
            switch (columnIndex) {
            case 0:
                return model.getName();
            case 1:
                return model.getType();
            case 2:
                return model.getSpec();
            case 3:
                return model.getStream();
            case 4:
                return model.getStatus();
            case 5:
                return numberFormatter.format(model.getDataCount());
            default:
                break;
            }
            return "";
        }

        @Override
        public Color getForeground(Object element) {
            if (!(element instanceof LinkInfo)) {
                return null;
            }

            if (index == 5) // cell status
            {
                try {
                    LinkInfo li = (LinkInfo) element;
                    if (li.getDisabled()) {
                        return UiColors.DISABLED_FAINT_FG;
                    } else if ("OK".equals(li.getStatus())) {
                        if (linksView.currentLinkModel.isDataCountIncreasing(li)) {
                            return UiColors.GOOD_BRIGHT_FG;
                        } else {
                            return UiColors.GOOD_FAINT_FG;
                        }
                    } else {
                        return UiColors.ERROR_FAINT_FG;
                    }
                } catch (Exception e) {
                    return null;
                }

            } else
                return null;
        }

        int index = 0;
        final int nbColumn = 6;

        // This one is called for each column, with the same LinkInfo element
        @Override
        public Color getBackground(Object element) {

            if (!(element instanceof LinkInfo)) {
                return null;
            }

            if (index == nbColumn)
                index = 0;
            index++;

            if (index == 5) // cell status
            {
                try {
                    LinkInfo li = (LinkInfo) element;
                    if (li.getDisabled()) {
                        return UiColors.DISABLED_FAINT_BG;
                    } else if ("OK".equals(li.getStatus())) {
                        if (linksView.currentLinkModel.isDataCountIncreasing(li)) {
                            return UiColors.GOOD_BRIGHT_BG;
                        } else {
                            return UiColors.GOOD_FAINT_BG;
                        }
                    } else {
                        return UiColors.ERROR_FAINT_BG;
                    }
                } catch (Exception e) {
                    return null;
                }

            } else
                return null;
        }
    }

}
