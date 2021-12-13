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
 * Multi dimensional array, which can be used for waveforms or more rich data.
 * <p>
 * The data is stored in a linear structure. The sizes array gives the dimensionality and size for each dimension. The
 * ordering defined by the sizes is {@code [..., zSize, ySize, xSize]}. Typical iteration is: <blockquote>
 * 
 * <pre>
 * for (...) {
 *   for (var z = 0; z &lt; zSize; z++) {
 *     for (var y = 0; y &lt; ySize; y++) {
 *       for (var x = 0; x &lt; xSize; x++) {
 *          array.getData().getDouble(... + z*ySize + y*xSize + x);
 *       }
 *     }
 *   }
 * }
 * </pre>
 * 
 * </blockquote>
 */
public interface Array {

    /**
     * Return the object containing the array data.
     * <p>
     * This method will either return a {@link List} or a {@link ListNumber} depending of the array type. A collection
     * is returned, instead of an array, so that the type implementation can be immutable or can at least try to prevent
     * modifications. ListNumber has also several advantages over the Java arrays, including the ability to iterate the
     * list regardless of numeric type.
     * <p>
     * If a numeric array is actually needed, refer to {@link CollectionNumbers}.
     *
     * @return the array data
     */
    Object getData();

    /**
     * The shape of the multidimensional array.
     * <p>
     * The size of the returned list will be the number of the dimension of the array. Each number represents the size
     * of each dimension. The total number of elements in the array is therefore the product of all the numbers in the
     * list returned.
     *
     * @return the dimension sizes
     */
    ListInt getSizes();
}
