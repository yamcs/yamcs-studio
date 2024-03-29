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
 * A sorted view of a list.
 */
public class SortedListView extends ListDouble {

    private ListNumber values;
    private ListInt indexes;
    private int[] indexArray;

    SortedListView(ListNumber values) {
        this.values = values;
        indexArray = new int[values.size()];
        for (var i = 0; i < indexArray.length; i++) {
            indexArray[i] = i;
        }
        indexes = new ArrayInt(indexArray);
    }

    SortedListView(ListNumber values, ListInt indexes) {
        this.values = values;
        this.indexes = indexes;
        indexArray = null;
    }

    @Override
    public double getDouble(int index) {
        return values.getDouble(indexes.getInt(index));
    }

    @Override
    public int size() {
        return values.size();
    }

    void exhange(int i, int j) {
        var b = indexArray[i];
        indexArray[i] = indexArray[j];
        indexArray[j] = b;
    }

    /**
     * Returns the index map of the sorted view.
     *
     * @return a list of integers
     */
    public ListInt getIndexes() {
        return indexes;
    }

    /**
     * Quick-sort the view. The original list is left alone, and the internal list is modified.
     *
     * @param list
     *            the view to sort
     */
    static void quicksort(SortedListView list) {
        quicksort(list, 0, list.size() - 1);
    }

    private static void quicksort(SortedListView list, int left, int right) {
        if (right <= left) {
            return;
        }
        var i = partition(list, left, right);
        quicksort(list, left, i - 1);
        quicksort(list, i + 1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(SortedListView list, int left, int right) {
        var i = left - 1;
        var j = right;
        while (true) {
            while (less(list.getDouble(++i), list.getDouble(right))) {
                // a[right] acts as sentinel
            }
            while (less(list.getDouble(right), list.getDouble(--j))) // find item on right to swap
            {
                if (j == left) {
                    break; // don't go out-of-bounds
                }
            }
            if (i >= j) {
                break; // check if pointers cross
            }
            list.exhange(i, j); // swap two elements into place
        }
        list.exhange(i, right); // swap with partition element
        return i;
    }

    // is x < y ?
    private static boolean less(double x, double y) {
        return (x < y);
    }
}
