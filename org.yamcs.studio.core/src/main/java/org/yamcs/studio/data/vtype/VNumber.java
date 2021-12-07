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

/**
 * Scalar number with alarm, timestamp, display and control information.
 * <p>
 * This class allows to use any scalar number (i.e. {@link VInt} or {@link VDouble}) through the same interface.
 */
public interface VNumber extends Scalar, Alarm, Time, Display, VType {

    /**
     * The numeric value.
     */
    @Override
    Number getValue();
}
