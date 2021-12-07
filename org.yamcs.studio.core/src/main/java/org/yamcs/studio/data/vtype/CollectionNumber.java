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

import java.util.Collection;

/**
 * A collection of numeric (primitive) elements. It provides a size and can be iterated more than once.
 * <p>
 * The method names are taken from {@link Collection}, though not all methods are specified. At this moment, the class
 * is read-only. If in the future the class is extended, the new methods should match the names from {@link Collection}.
 */
public interface CollectionNumber {

    /**
     * Returns an iterator over the elements of the collection.
     */
    IteratorNumber iterator();

    /**
     * Returns the number of elements in the collection.
     */
    int size();
}
