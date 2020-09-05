package org.yamcs.studio.data.vtype;

/**
 * Scalar number with alarm, timestamp, display and control information.
 * <p>
 * This class allows to use any scalar number (i.e. {@link VInt} or {@link VDouble}) through the same interface.
 */
public interface VNumber extends Scalar, Alarm, Time, Display, VType {

    /**
     * The numeric value.
     */
    @Override
    Number getValue();
}
