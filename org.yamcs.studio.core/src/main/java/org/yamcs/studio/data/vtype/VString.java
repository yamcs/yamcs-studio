package org.yamcs.studio.data.vtype;

/**
 * Scalar string with alarm and timestamp.
 */
public interface VString extends Scalar, Alarm, Time, VType {

    @Override
    String getValue();
}
