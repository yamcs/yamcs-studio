package org.yamcs.studio.data.vtype;

/**
 * Byte array with alarm, timestamp, display and control information.
 */
public interface VByteArray extends VNumberArray, VType {

    @Override
    ListByte getData();
}
