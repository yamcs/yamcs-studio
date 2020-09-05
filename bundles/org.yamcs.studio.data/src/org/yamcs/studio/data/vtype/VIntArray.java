package org.yamcs.studio.data.vtype;

/**
 * Int array with alarm, timestamp, display and control information.
 */
public interface VIntArray extends VNumberArray, VType {

    @Override
    ListInt getData();
}
