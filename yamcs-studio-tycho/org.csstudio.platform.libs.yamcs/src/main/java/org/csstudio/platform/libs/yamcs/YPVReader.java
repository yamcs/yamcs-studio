package org.csstudio.platform.libs.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;

public interface YPVReader {

    public void signalYamcsConnected();

    public void signalYamcsDisconnected();

    public void reportException(Exception e);

    public String getPVName();

    public void processParameterValue(ParameterValue pval);
}
