/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Scalar enum with alarm and timestamp.
 * Given that enumerated values are of very limited use without
 * the labels, and that the current label is the data most likely used, the
 * enum is scalar of type {@link String}. The index is provided as an extra field, and
 * the list of all possible values is always provided.
 *
 * @author carcassi
 */
public abstract class VEnum extends Scalar {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract String getValue();

    /**
     * Return the index of the value in the list of labels.
     *
     * @return the current index
     */
    public abstract int getIndex();

    /**
     * Returns the metadata, including all possible labels.
     *
     * @return the metadata
     */
    public abstract EnumMetaData getMetaData();

    /**
     * Create a new VEnum.
     *
     * @param index the index in the label array
     * @param metaData the metadata
     * @param alarm the alarm
     * @param time the time
     * @return the new value
     */
    public static VEnum newVEnum(int index, EnumMetaData metaData, Alarm alarm, Time time) {
        return new IVEnum(index, metaData, alarm, time);
    }

}
