package org.yamcs.studio.data.vtype;

/**
 * Cell boundaries and unit information needed for array display.
 * <p>
 * For a given numeric dimension, it provides the cell boundaries and the unit in terms of which the boundaries are
 * expressed.
 */
public interface ArrayDimensionDisplay {

    /**
     * Returns the boundaries of the cell in the given unit.
     */
    ListNumber getCellBoundaries();

    /**
     * String representation of the units using for all values. Never null. If not available, returns the empty String.
     */
    String getUnits();

    /**
     * Whether the values for this dimension are organized in the opposite order.
     *
     * @return true if values are stored in the array in the reverse order
     */
    boolean isReversed();
}
