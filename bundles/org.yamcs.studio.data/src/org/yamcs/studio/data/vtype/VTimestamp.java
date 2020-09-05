package org.yamcs.studio.data.vtype;

import java.util.Date;

public interface VTimestamp extends Scalar, Alarm, Time, Display, VType {

    @Override
    Date getValue();
}
