package org.csstudio.utility.pvmanager.yamcs.service;

import org.yamcs.protobuf.ParameterValue;

public interface YPVListener {

    public void signalYamcsConnected();
    public void signalYamcsDisconnected();
    public void reportException(Exception e);
    public String getPVName();
    public void processParameterValue(ParameterValue pval);
}
