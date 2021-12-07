/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

import java.time.Instant;

/**
 * Time information.
 */
public interface Time {

    /**
     * The time instant of the value, typically indicating when it was generated. If never connected, it returns the
     * time when it was last determined that no connection was made.
     */
    Instant getTimestamp();

    /**
     * Returns a user defined tag, that can be used to store extra time information, such as beam shot.
     */
    Integer getTimeUserTag();

    /**
     * Returns a data source specific flag to indicate whether the time information should be trusted. Typical cases are
     * when records were not processes and the timestamp has a zero time.
     *
     * @return true if the time information is valid
     */
    boolean isTimeValid();
}
