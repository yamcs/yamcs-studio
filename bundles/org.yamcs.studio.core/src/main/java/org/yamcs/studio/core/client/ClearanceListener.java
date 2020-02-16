package org.yamcs.studio.core.client;

import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;

@FunctionalInterface
public interface ClearanceListener {

    void clearanceChanged(boolean enabled, SignificanceLevelType level);
}
