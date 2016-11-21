package org.yamcs.studio.ui.links;

import org.yamcs.protobuf.YamcsManagement.LinkInfo;

/**
 * Wrapper around LinkInfo with some extra metadata for use in the table
 */
public class DataLinkRecord {

    private LinkInfo linkInfo;
    private long lastDataCountIncrease;

    public DataLinkRecord(LinkInfo linkInfo) {
        this.linkInfo = linkInfo;
    }

    public LinkInfo getLinkInfo() {
        return linkInfo;
    }

    public boolean isDataCountIncreasing() {
        return (System.currentTimeMillis() - lastDataCountIncrease) < 1500;
    }

    public void processIncomingLinkInfo(LinkInfo incoming) {
        // In git history you'll find we had a 2s timeout future here
        // that registered a refresh of the row, such that the bg color
        // would fade. It was commented out, so I removed it.
        // Maybe because there's enough refreshes of a table to not need it.
        // But to be verified.
        if (incoming.getDataCount() > linkInfo.getDataCount()) {
            lastDataCountIncrease = System.currentTimeMillis();
        }
        linkInfo = incoming;
    }
}
