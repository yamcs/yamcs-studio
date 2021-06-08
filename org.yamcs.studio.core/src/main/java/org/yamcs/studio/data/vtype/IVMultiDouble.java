package org.yamcs.studio.data.vtype;

import java.util.List;

/**
 * Immutable VMultiDouble implementation.
 */
public class IVMultiDouble extends IVNumeric implements VMultiDouble {

    private final List<VDouble> values;

    IVMultiDouble(List<VDouble> values, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.values = values;
    }

    @Override
    public List<VDouble> getValues() {
        return values;
    }
}
