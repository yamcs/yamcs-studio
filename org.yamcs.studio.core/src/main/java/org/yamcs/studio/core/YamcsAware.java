/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core;

import java.time.Instant;

import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.ProcessorInfo;

/**
 * Marks a component as being aware of the global UI state. This state includes the connected instance and/or processor.
 */
public interface YamcsAware {

    default void onYamcsConnecting() {
    }

    default void onYamcsConnectionFailed(Throwable t) {
    }

    /**
     * Called when we the global connection to yamcs was succesfully established
     */
    default void onYamcsConnected() {
    }

    /**
     * Called when the yamcs is connection went lost
     */
    default void onYamcsDisconnected() {
    }

    /**
     * The globally activated instance has changed. This is always called on the UI thread.
     */
    default void changeInstance(String instance) {
    }

    /**
     * The globally activated processor has changed. This is always called on the UI thread.
     * <p>
     * Note that this method is not called if only the instance has changed.
     */
    default void changeProcessor(String instance, String processor) {
    }

    default void changeProcessorInfo(ProcessorInfo processor) {
    }

    /**
     * Actual mission time as reported by Yamcs Server. If no time is defined (for example when not connected),
     * listeners will receive null
     */
    default void updateTime(Instant time) {
    }

    default void updateClearance(boolean enabled, SignificanceLevelType level) {
    }
}
