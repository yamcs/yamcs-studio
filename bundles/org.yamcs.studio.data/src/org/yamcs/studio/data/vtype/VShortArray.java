package org.yamcs.studio.data.vtype;

/**
 * Short array with alarm, timestamp, display and control information.
 */
public interface VShortArray extends VNumberArray, VType {

    @Override
    ListShort getData();
}
