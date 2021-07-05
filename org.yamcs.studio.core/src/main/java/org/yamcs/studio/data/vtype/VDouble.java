package org.yamcs.studio.data.vtype;

/**
 * Scalar double with alarm, timestamp, display and control information. Auto-unboxing makes the extra method for the
 * primitive type unnecessary.
 */
public interface VDouble extends VNumber, VType {

    @Override
    Double getValue();
}
