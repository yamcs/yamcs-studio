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

import java.util.List;

/**
 * Multi channel array.
 * <p>
 * This same type can be used in multiple circumstances where data is collected, regardless of how and what of data. The
 * number of values in the multi channel array never changes.
 * <ul>
 * <li>Synchronized array: {@link Time} returns the reference time of the collected data); each element may or may not
 * retain its time information.</li>
 * <li>Multi channel: {@link Time} returns the time of the data generation; each element may or may not retain its time
 * information</li>
 * </ul>
 * <p>
 * A {@link MultiScalar} can be automatically converted to a {@link Array} of the same type.
 *
 * @param <T>
 *            the type for the multi channel values
 */
public interface MultiScalar<T extends Scalar> {

    /**
     * The list of values for all the different channels. Never null.
     *
     * @return a {@link List} of values
     */
    List<T> getValues();
}
