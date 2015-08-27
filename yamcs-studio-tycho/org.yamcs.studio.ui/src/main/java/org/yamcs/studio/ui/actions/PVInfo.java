package org.yamcs.studio.ui.actions;

import org.csstudio.simplepv.IPV;
import org.yamcs.protobuf.Rest.RestParameterInfo;

public class PVInfo implements Comparable<PVInfo> {

    private String displayName;
    private IPV pv;

    // In case it's a yamcs parameter, one of these two should be filled in
    private RestParameterInfo parameterInfo;
    private String parameterInfoException;

    public PVInfo(String displayName, IPV pv) {
        this.displayName = displayName;
        this.pv = pv;
    }

    public RestParameterInfo getParameterInfo() {
        return parameterInfo;
    }

    public void setParameterInfo(RestParameterInfo restParameterInfo) {
        this.parameterInfo = restParameterInfo;
    }

    public void setParameterInfoException(String message) {
        parameterInfoException = message;
    }

    public String getParameterInfoException() {
        return parameterInfoException;
    }

    public String getDisplayName() {
        return displayName;
    }

    public IPV getPV() {
        return pv;
    }

    public boolean isYamcsParameter() {
        return !displayName.contains("://");
    }

    @Override
    public int compareTo(PVInfo other) {
        // TODO yamcs should be first
        return displayName.compareTo(other.displayName);
    }
}
