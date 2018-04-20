package org.diirt.vtype;

import java.util.Date;

public interface VTimestamp extends Scalar, Alarm, Time, Display, VType {

    @Override
    Date getValue();
}
