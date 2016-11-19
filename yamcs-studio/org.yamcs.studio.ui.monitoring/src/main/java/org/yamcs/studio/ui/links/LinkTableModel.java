package org.yamcs.studio.ui.links;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.yamcs.protobuf.YamcsManagement.LinkInfo;

class LinkTableModel {
    private ArrayList<LinkInfo> links = new ArrayList<LinkInfo>();
    private ArrayList<Long> lastDataCountIncrease = new ArrayList<Long>();
    private ArrayList<ScheduledFuture<?>> schduledFutures = new ArrayList<ScheduledFuture<?>>();

    private LinksTableViewer linksTableViewer;

    ScheduledThreadPoolExecutor timer;
    private static final Logger log = Logger.getLogger(LinkTableModel.class.getName());

    public LinkTableModel(ScheduledThreadPoolExecutor timer, LinksTableViewer linksTableViewer) {
        this.linksTableViewer = linksTableViewer;
        this.timer = timer;
    }

    /**
     * schedule a fire rows updated , to change the color of the line if no data has been received
     * in the last two seconds
     *
     * @param row
     */
    private void scheduleFireTableRowsUpdated(final int row) {
        ScheduledFuture<?> future = schduledFutures.get(row);
        if (future != null)
            future.cancel(false);
        future = timer.schedule(() -> {
            if (linksTableViewer.getTable().isDisposed())
                return;
            linksTableViewer.getTable().getDisplay().asyncExec(() -> {
                if (linksTableViewer.getTable().isDisposed())
                    return;
                // fireTableRowsUpdated(row, row);
            });
        } , 2, TimeUnit.SECONDS);
        schduledFutures.set(row, future);
    }

    public boolean isDataCountIncreasing(LinkInfo li) {
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i) == li)
                return (System.currentTimeMillis() - lastDataCountIncrease.get(i)) < 1500;
        }
        log.severe("Unable to find the datalink in the model. " + li);
        return false;

    }

    public void updateLink(LinkInfo uli) {
        boolean found = false;
        for (int i = 0; i < links.size(); ++i) {
            LinkInfo li = links.get(i);
            if (li.getName().equals(uli.getName())) {
                if (uli.getDataCount() > li.getDataCount()) {
                    lastDataCountIncrease.set(i, System.currentTimeMillis());
                    scheduleFireTableRowsUpdated(i);
                }
                links.set(i, uli);
                if (linksTableViewer.getTable().isDisposed())
                    return;
                linksTableViewer.replace(uli, i);
                found = true;
                break;
            }
        }
        if (!found) {
            links.add(uli);
            lastDataCountIncrease.add(0L);
            schduledFutures.add(null);
            linksTableViewer.add(uli);
        }

    }

}
