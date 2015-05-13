package org.yamcs.studio.core;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

public interface YamcsPVReader {

    void reportException(Exception e);

    String getMdbNamespace();

    String getPVName();

    void processParameterValue(ParameterValue pval);

    void processConnectionInfo(PVConnectionInfo info);

    default NamedObjectList toNamedObjectList() {
        return NamedObjectList.newBuilder().addList(NamedObjectId.newBuilder()
                .setNamespace(getMdbNamespace())
                .setName(getPVName())).build();
    }

    default NamedObjectId toNamedObjectId() {
        return NamedObjectId.newBuilder()
                .setNamespace(getMdbNamespace())
                .setName(getPVName()).build();
    }
}
