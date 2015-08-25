package org.yamcs.studio.core;

import org.yamcs.protobuf.Yamcs.TimeInfo;

/**
 * Reports on time as indicated by the studio-wide processor
 */
public interface TimeListener {

    public void processTime(TimeInfo timeInfo);

}
