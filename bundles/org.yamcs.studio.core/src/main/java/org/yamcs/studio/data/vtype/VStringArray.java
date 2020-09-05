package org.yamcs.studio.data.vtype;

import java.util.List;

public interface VStringArray extends Array, Alarm, Time, VType {

    @Override
    List<String> getData();
}
