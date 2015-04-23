package org.yamcs.studio.core;

import org.yamcs.protobuf.Commanding.CommandHistoryEntry;

public interface CommandHistoryListener {

    public void signalYamcsConnected();

    public void signalYamcsDisconnected();

    // TODO public void reportException(Exception e); // need?
    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry);
}
