package org.csstudio.platform.libs.yamcs;

import org.yamcs.protostuff.CommandHistoryEntry;

public interface CommandHistoryListener {

    public void signalYamcsConnected();
    public void signalYamcsDisconnected();
    // TODO public void reportException(Exception e); // need?
    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry);
}
