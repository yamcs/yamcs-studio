package org.yamcs.studio.data.vtype;

/**
 * Long array with alarm, timestamp, display and control information.
 */
public interface VLongArray extends VNumberArray, VType {

    @Override
    ListLong getData();
}
