package org.yamcs.studio.data.vtype;

/**
 * Double array with alarm, timestamp, display and control information.
 */
public interface VDoubleArray extends VNumberArray, VType {

    @Override
    ListDouble getData();
}
