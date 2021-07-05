package org.yamcs.studio.data.vtype;

/**
 * Scalar long with alarm, timestamp, display and control information. Auto-unboxing makes the extra method for the
 * primitive type unnecessary.
 */
public interface VLong extends VNumber, VType {

    @Override
    Long getValue();
}
