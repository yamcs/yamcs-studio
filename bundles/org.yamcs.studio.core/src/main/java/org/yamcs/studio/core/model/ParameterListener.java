package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Pvalue.ParameterData;

/**
 * Reports on parameters in the studio-wide instance
 */
public interface ParameterListener {

    /**
     * Called when the MDB was updated
     */
    void mdbUpdated();

    /**
     * Called upon incoming parameter data
     */
    void onParameterData(ParameterData pdata);
}
