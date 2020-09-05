package org.yamcs.studio.data.vtype;

/**
 * Scalar byte with alarm, timestamp, display and control information. Auto-unboxing makes the extra method for the
 * primitive type unnecessary.
 */
public interface VByte extends VNumber, VType {

    @Override
    Byte getValue();
}
