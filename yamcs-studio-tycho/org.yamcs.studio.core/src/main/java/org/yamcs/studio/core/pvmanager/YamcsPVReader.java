package org.yamcs.studio.core.pvmanager;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

public interface YamcsPVReader {

    void reportException(Exception e);

    NamedObjectId getId();

    void processParameterValue(ParameterValue pval);

    void processConnectionInfo(PVConnectionInfo info);

    default NamedObjectList toNamedObjectList() {
        return NamedObjectList.newBuilder().addList(getId()).build();
    }
}
