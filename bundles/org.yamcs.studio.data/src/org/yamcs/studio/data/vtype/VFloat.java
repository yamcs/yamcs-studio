package org.yamcs.studio.data.vtype;

/**
 * Scalar float with alarm, timestamp, display and control information. Auto-unboxing makes the extra method for the
 * primitive type unnecessary.
 */
public interface VFloat extends VNumber, VType {

    @Override
    Float getValue();
}
