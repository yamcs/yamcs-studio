package org.yamcs.studio.css.pvmanager;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

/**
 * Represents a web socket parameter subscription for one specific PV
 */
public interface YamcsPVReader {

    void reportException(Exception e);

    NamedObjectId getId();

    /**
     *
     * Called when you get MDB information on the parameter
     *
     * @param id
     *            an id that was subscribed to
     * @param info
     *            the received info
     */
    void processConnectionInfo(PVConnectionInfo info);

    default NamedObjectList toNamedObjectList() {
        return NamedObjectList.newBuilder().addList(getId()).build();
    }

    void processParameterValue(ParameterValue pval);
}
