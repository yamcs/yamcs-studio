/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Immutable VEnum implementation.
 *
 * @author carcassi
 */
class IVEnum extends VEnum {

    private final Alarm alarm;
    private final Time time;
    private final int index;
    private final EnumMetaData metadata;

    IVEnum(int index, EnumMetaData metadata, Alarm alarm, Time time) {
        if (index < 0 || index >= metadata.getLabels().size()) {
            throw new IndexOutOfBoundsException("VEnum index must be within the label range");
        }
        this.index = index;
        this.metadata = metadata;
        this.alarm = alarm;
        this.time = time;
    }

    @Override
    public String getValue() {
        return metadata.getLabels().get(index);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public EnumMetaData getMetaData() {
        return metadata;
    }

    @Override
    public Alarm getAlarm() {
        return alarm;
    }

    @Override
    public Time getTime() {
        return time;
    }

}
