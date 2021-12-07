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
 * Scalar enum with alarm and timestamp. Given that enumerated values are of very limited use without the labels, and
 * that the current label is the data most likely used, the enum is of type {@link String}. The index is provided as an
 * extra field, and the list of all possible values is always provided.
 */
public interface VEnum extends Scalar, Enum, Alarm, Time, VType {

    @Override
    String getValue();

    /**
     * Return the index of the value in the list of labels.
     *
     * @return the current index
     */
    int getIndex();

}
