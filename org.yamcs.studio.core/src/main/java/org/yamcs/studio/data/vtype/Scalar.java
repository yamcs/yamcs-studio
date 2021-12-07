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
 * Basic type definition for all scalar types. {@link #getValue()} never returns null, even if the channel never
 * connected. One <b>must always look</b> at the alarm severity to be able to correctly interpret the value.
 * <p>
 * As of 1.1, this class is not a generic type. This is due to a bug in 1.6 compiler where generic return type clash
 * with covariant return types.
 */
public interface Scalar {

    /**
     * Returns the value. Never null.
     */
    Object getValue();
}
