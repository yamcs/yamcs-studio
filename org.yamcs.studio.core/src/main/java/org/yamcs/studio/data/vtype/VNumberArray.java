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
