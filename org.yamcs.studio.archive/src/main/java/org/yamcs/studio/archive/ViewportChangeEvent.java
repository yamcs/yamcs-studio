/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.archive;

import java.time.OffsetDateTime;

public class ViewportChangeEvent {

    private OffsetDateTime start;
    private OffsetDateTime stop;

    public ViewportChangeEvent(OffsetDateTime start, OffsetDateTime stop) {
        this.start = start;
        this.stop = stop;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getStop() {
        return stop;
    }
}
