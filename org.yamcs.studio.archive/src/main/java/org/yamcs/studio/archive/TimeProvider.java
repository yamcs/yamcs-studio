package org.yamcs.studio.archive;

import java.time.OffsetDateTime;

@FunctionalInterface
public interface TimeProvider {

    OffsetDateTime getTime();
}
