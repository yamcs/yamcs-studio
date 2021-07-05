package org.yamcs.studio.commanding.cmdhist;

import org.yamcs.client.Acknowledgment;

public class AckTableRecord {

    Acknowledgment acknowledgment;
    CommandHistoryRecord rec;

    AckTableRecord(Acknowledgment acknowledgment, CommandHistoryRecord rec) {
        this.acknowledgment = acknowledgment;
        this.rec = rec;
    }
}
