package org.yamcs.studio.core;

import org.yamcs.protobuf.Commanding.CommandHistoryEntry;

public interface CommandHistoryListener {

    // TODO public void reportException(Exception e); // need?
    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry);
}
