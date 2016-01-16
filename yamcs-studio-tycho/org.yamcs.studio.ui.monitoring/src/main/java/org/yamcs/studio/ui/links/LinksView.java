package org.yamcs.studio.ui.links;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.YamcsManagement.LinkInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.model.LinkListener;
import org.yamcs.utils.TimeEncoding;

public class LinksView extends ViewPart implements StudioConnectionListener, LinkListener {

    private static final Logger log = Logger.getLogger(LinksView.class.getName());
    ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);

    LinksTableViewer linksTableViewer;
    LinkContentProvider linksContentProvider;

    HashMap<String, LinkTableModel> linkModels = new HashMap<String, LinkTableModel>();
    LinkTableModel currentLinkModel;

    @Override
    public void createPartControl(Composite parent) {
        // Build the tables
        FillLayout fl = new FillLayout();
        fl.marginHeight = 0;
        fl.marginWidth = 0;
        parent.setLayout(fl);

        Composite tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        linksTableViewer = new LinksTableViewer(this, tableWrapper, tcl);
        linksContentProvider = new LinkContentProvider(linksTableViewer);
        linksTableViewer.setContentProvider(linksContentProvider);
        linksTableViewer.setInput(linksContentProvider);

        linksTableViewer.getTable().addListener(SWT.FocusOut, (event) -> {
            linksTableViewer.getTable().deselectAll();
        });

        if (getViewSite() != null)
            getViewSite().setSelectionProvider(linksTableViewer);

        // Set initial state
        linksTableViewer.refresh();

        LinkCatalogue.getInstance().addLinkListener(this);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    @Override
    public void linkRegistered(LinkInfo linkInfo) {
        linkUpdated(linkInfo);
    }

    @Override
    public void linkUpdated(LinkInfo li) {
        if (linksTableViewer.getTable().isDisposed())
            return;

        Display display = linksTableViewer.getTable().getDisplay();
        display.asyncExec(() -> {
            if (display.isDisposed())
                return;

            log.fine("processing updateLink " + li);
            String modelName = li.getInstance();
            if (!linkModels.containsKey(modelName)) {
                addLinkModel(li.getInstance());
            }
            String currentYamcsInstance = ConnectionManager.getInstance().getYamcsInstance();
            if (currentYamcsInstance != null && currentYamcsInstance.equals(modelName)) {
                LinkTableModel model = linkModels.get(modelName);
                model.updateLink(li);
            }
        });
    }

    @Override
    public void linkUnregistered(LinkInfo linkInfo) {
        // TODO but not currently sent by Yamcs
    }

    public void addLinkModel(String instance) {
        LinkTableModel model = new LinkTableModel(timer, linksTableViewer);
        linkModels.put(instance, model);
        if (currentLinkModel == null)
            currentLinkModel = model;
    }

    @Override
    public void onStudioConnect() {
    }

    @Override
    public void onStudioDisconnect() {
        Display.getDefault().asyncExec(() -> {
            this.linksTableViewer.getTable().removeAll();
            this.currentLinkModel = null;
            this.linkModels.clear();
        });
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        timer.shutdown();
        super.dispose();
    }

    public static void main(String[] arg) {
        TimeEncoding.setUp();
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("dialog test");
        shell.open();

        LinksView lv = new LinksView();
        lv.createPartControl(shell);
        shell.pack();

        lv.onStudioConnect();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

}
