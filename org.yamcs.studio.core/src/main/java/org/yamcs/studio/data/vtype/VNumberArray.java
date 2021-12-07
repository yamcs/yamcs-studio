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
 * Numeric array with alarm, timestamp, display and control information.
 * <p>
 * This class allows to use any numeric array (i.e. {@link VIntArray} or {@link VDoubleArray}) through the same
 * interface.
 */
public interface VNumberArray extends Array, Alarm, Time, Display, VType {

    @Override
    ListNumber getData();

    /**
     * Returns the boundaries of each cell.
     *
     * @return the dimension display; can't be null
     */
    List<ArrayDimensionDisplay> getDimensionDisplay();
}
