/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

/**
 * A listener on all events of the PV.
 */
public interface IPVListener {

    /**
     * Will be called when connection state changed. It the connection is closed by explicitly calling
     * {@link IPV#stop()}, this method will not be notified.
     *
     * @param pv
     *            the pv whose connection state changed.
     */
    default void connectionChanged(IPV pv) {
    }

    /**
     * If no {@link ExceptionHandler} was given to the PV, this method will be called when exception happened.
     * Otherwise, the exception will be handled by the {@link ExceptionHandler}.
     *
     * @param pv
     *            the pv which has read related exception happened.
     * @param exception
     *            the exception that has been caught.
     */
    default void exceptionOccurred(IPV pv, Exception exception) {
    }

    /**
     * Will be called when PV value changed.
     *
     * @param pv
     *            the pv whose value has changed.
     */
    default void valueChanged(IPV pv) {
    }

    /**
     * Will be called when a write is finished. <br>
     * <b>Note:</b> when this is called, the value of the pv may not update yet, which depends the max update rate, so
     * it is not recommended to call {@link IPV#getValue()} in this method.
     *
     * @param pv
     *            the pv on which the write event happened.
     * @param isWriteSucceeded
     *            true if the write was successful.
     */
    default void writeFinished(IPV pv, boolean isWriteSucceeded) {
    }

    /**
     * Will be called when write permission <b>may</b> have changed. The write permission can be get from
     * {@link IPV#isWriteAllowed()}
     *
     * @param pv
     *            the pv whose permission may have changed.
     */
    default void writePermissionChanged(IPV pv) {
    }
}
