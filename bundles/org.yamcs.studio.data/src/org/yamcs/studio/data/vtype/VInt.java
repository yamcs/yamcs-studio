package org.yamcs.studio.data.vtype;

/**
 * Scalar integer with alarm, timestamp, display and control information. Auto-unboxing makes the extra method for the
 * primitive type unnecessary.
 */
public interface VInt extends VNumber, VType {

    @Override
    Integer getValue();
}
