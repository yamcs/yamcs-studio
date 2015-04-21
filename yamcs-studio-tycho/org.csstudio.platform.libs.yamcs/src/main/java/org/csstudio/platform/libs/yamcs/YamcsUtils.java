package org.csstudio.platform.libs.yamcs;

import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

public class YamcsUtils {

    public static NamedObjectList toNamedObjectList(String pvName) {
        return NamedObjectList.newBuilder().addList(NamedObjectId.newBuilder()
                .setNamespace(YamcsPlugin.getDefault().getMdbNamespace())
                .setName(pvName)).build();
    }

    public static NamedObjectId toNamedObjectId(String pvName) {
        return NamedObjectId.newBuilder()
                .setNamespace(YamcsPlugin.getDefault().getMdbNamespace())
                .setName(pvName).build();
    }
}
