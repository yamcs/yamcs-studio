package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Archive.StreamData;

/**
 * Reports on streams in the studio-wide instance
 */
public interface StreamListener {

    void processStreamData(StreamData data);
}
