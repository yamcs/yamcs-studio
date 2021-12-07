package org.yamcs.studio.links;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.client.LinkSubscription;
import org.yamcs.client.MessageListener;
import org.yamcs.protobuf.LinkEvent;
import org.yamcs.protobuf.SubscribeLinksRequest;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;

public class LinksView extends ViewPart implements YamcsAware, MessageListener<LinkEvent> {

    private LinkTableViewer tableViewer;
    private LinkTableViewerContentProvider contentProvider;

    private LinkSubscription subscription;

    @Override
    public void createPartControl(Composite parent) {
        var tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        var tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        tableViewer = new LinkTableViewer(tableWrapper, tcl);
        contentProvider = new LinkTableViewerContentProvider(tableViewer);
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(contentProvider);

        tableViewer.getTable().addListener(SWT.MouseDown, evt -> {
            // Allow the user to get rid of a selection in small tables
            // Note: before this was registered to a FocusOut, but that
            // broke the popup menu for me.
            if (tableViewer.getCell(new Point(evt.x, evt.y)) == null) {
                tableViewer.getTable().deselectAll();
            }
        });

        if (getViewSite() != null) {
            getViewSite().setSelectionProvider(tableViewer);
        }

        // Set initial state
        tableViewer.refresh();

        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeInstance(String instance) {
        if (subscription != null) {
            subscription.cancel(true);
        }

        Display.getDefault().asyncExec(() -> {
            tableViewer.getTable().removeAll();
            contentProvider.clearAll();

            if (instance != null) {
                subscription = YamcsPlugin.getYamcsClient().createLinkSubscription();
                subscription.addMessageListener(this);
                subscription.sendMessage(SubscribeLinksRequest.newBuilder().setInstance(instance).build());
            }
        });
    }

    @Override
    public void onMessage(LinkEvent linkEvent) {
        if (tableViewer.getTable().isDisposed()) {
            return;
        }

        var display = tableViewer.getTable().getDisplay();
        display.asyncExec(() -> {
            if (display.isDisposed()) {
                return;
            }

            switch (linkEvent.getType()) {
            case REGISTERED:
            case UPDATED:
                contentProvider.processLinkInfo(linkEvent.getLinkInfo());
                break;
            case UNREGISTERED:
                // TODO but not currently sent by Yamcs
            }
        });
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.cancel(true);
        }
        YamcsPlugin.removeListener(this);
        super.dispose();
    }

    @Override
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }
}
