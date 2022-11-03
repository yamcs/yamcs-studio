package org.yamcs.studio.css.core;

import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.data.IPV;

public class PVInfo implements Comparable<PVInfo> {

    private String displayName;
    private IPV pv;

    // In case it's a yamcs parameter, one of these two should be filled in
    private ParameterInfo parameterInfo;
    private String parameterInfoException;

    public PVInfo(String displayName, IPV pv) {
        this.displayName = displayName;
        this.pv = pv;
    }

    public ParameterInfo getParameterInfo() {
        return parameterInfo;
    }

    public String getPVType() {
        if (displayName.startsWith("para://") || displayName.startsWith("raw://") || displayName.startsWith("ops://")) {
            return "Yamcs Parameter";
        } else if (displayName.startsWith("loc://")) {
            return "Local PV";
        } else if (displayName.startsWith("sim://")) {
            return "Locally Simulated PV";
        } else if (displayName.startsWith("sys://")) {
            return "Local System Indicator";
        } else if (displayName.startsWith("=")) {
            return "Formula";
        } else if (isYamcsParameter()) {
            return "Yamcs Parameter";
        } else {
            return "Unknown";
        }
    }

    public void setParameterInfo(ParameterInfo parameterInfo) {
        this.parameterInfo = parameterInfo;
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
        if (displayName.startsWith("para://")
                || displayName.startsWith("raw://")
                || displayName.startsWith("ops://")) {
            return true;
        } else if (displayName.startsWith("=")) {
            return false;
        } else {
            return !(displayName.contains("://")); // default schema
        }
    }

    @Override
    public int compareTo(PVInfo other) {
        return displayName.compareTo(other.displayName);
    }
}
