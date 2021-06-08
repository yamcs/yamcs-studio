package org.yamcs.studio.data.vtype;

/**
 * Byte array with alarm, timestamp, display and control information.
 */
public interface VBooleanArray extends Array, Alarm, Time, VType {

    @Override
    ListBoolean getData();
}
