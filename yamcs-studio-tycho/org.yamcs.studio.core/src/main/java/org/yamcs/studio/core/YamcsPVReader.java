package org.yamcs.studio.core;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

public interface YamcsPVReader {

    void reportException(Exception e);

    String getPVName();

    void processParameterValue(ParameterValue pval);

    void processConnectionInfo(PVConnectionInfo info);

    default String getMdbNamespace() {
        if (getPVName().charAt(0) == '/')
            return null;
        else
            return YamcsPlugin.getDefault().getMdbNamespace();
    }

    default NamedObjectList toNamedObjectList() {
        return NamedObjectList.newBuilder().addList(toNamedObjectId()).build();
    }

    default NamedObjectId toNamedObjectId() {
        NamedObjectId.Builder builder = NamedObjectId.newBuilder().setName(getPVName());
        String mdbNamespace = getMdbNamespace();
        if (mdbNamespace != null)
            builder.setNamespace(mdbNamespace);
        return builder.build();
    }
}
