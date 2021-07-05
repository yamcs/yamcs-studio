package org.yamcs.studio.data.vtype;

import java.util.List;

public interface VEnumArray extends Array, Enum, Alarm, Time, VType {

    @Override
    List<String> getData();

    /**
     * Returns the indexes instead of the labels.
     *
     * @return an array of indexes
     */
    ListInt getIndexes();
}
