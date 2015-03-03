package org.csstudio.platform.libs.yamcs;

import org.yamcs.protostuff.ParameterValue;

public interface YPVListener {

    public void signalYamcsConnected();
    public void signalYamcsDisconnected();
    public void reportException(Exception e);
    public String getPVName();
    public void processParameterValue(ParameterValue pval);
}
