package org.yamcs.studio.data.vtype;

/**
 * Scalar short with alarm, timestamp, display and control information. Auto-unboxing makes the extra method for the
 * primitive type unnecessary.
 */
public interface VShort extends VNumber, VType {

    @Override
    Short getValue();
}
