package org.csstudio.platform.libs.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;

public interface YamcsPVReader {

    public void reportException(Exception e);

    public String getPVName();

    public void processParameterValue(ParameterValue pval);

    public void processConnectionInfo(PVConnectionInfo info);
}
