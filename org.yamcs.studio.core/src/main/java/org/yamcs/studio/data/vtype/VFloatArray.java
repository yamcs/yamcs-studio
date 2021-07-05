package org.yamcs.studio.data.vtype;

/**
 * Float array with alarm, timestamp, display and control information.
 */
public interface VFloatArray extends VNumberArray, VType {

    @Override
    ListFloat getData();
}
