package org.yamcs.studio.data.vtype;

/**
 * Scalar boolean with alarm and timestamp.
 */
public interface VBoolean extends Scalar, Alarm, Time, VType {

    @Override
    Boolean getValue();
}
